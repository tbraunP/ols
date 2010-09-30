/*
 * OpenBench LogicSniffer / SUMP project 
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *
 * Copyright (C) 2006-2010 Michael Poppitz, www.sump.org
 * Copyright (C) 2010 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.ols.client.diagram.laf;


import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;

import nl.lxtreme.ols.api.data.*;
import nl.lxtreme.ols.client.diagram.*;
import nl.lxtreme.ols.client.diagram.settings.*;
import nl.lxtreme.ols.util.*;


/**
 * Provides the Look&Feel for the diagram timeline component.
 */
public class DiagramTimeLineUI extends ComponentUI
{
  // CONSTANTS

  /** The tick increment (in pixels). */
  public static final int TIMELINE_INCREMENT = 20;
  /** The height of this component. */
  public static final int TIMELINE_HEIGHT = 30;

  private static final int LONG_TICK_INTERVAL = 10;
  private static final int TIME_INTERVAL = 20;

  private static final int SHORT_TICK_HEIGHT = 4;
  private static final int PADDING_Y = 1;

  // VARIABLES

  private Font labelFont;

  // METHODS

  /**
   * @see javax.swing.plaf.ComponentUI#getMaximumSize(javax.swing.JComponent)
   */
  @Override
  public Dimension getMaximumSize( final JComponent aC )
  {
    return new Dimension( Short.MAX_VALUE, Short.MAX_VALUE );
  }

  /**
   * @see javax.swing.plaf.ComponentUI#getMinimumSize(javax.swing.JComponent)
   */
  @Override
  public Dimension getMinimumSize( final JComponent aComponent )
  {
    return new Dimension( 0, TIMELINE_HEIGHT );
  }

  /**
   * @see javax.swing.plaf.ComponentUI#getPreferredSize(javax.swing.JComponent)
   */
  @Override
  public Dimension getPreferredSize( final JComponent aComponent )
  {
    final DiagramTimeLine timeline = ( DiagramTimeLine )aComponent;
    final Diagram diagram = timeline.getDiagram();
    return new Dimension( diagram.getPreferredSize().width, TIMELINE_HEIGHT );
  }

  /**
   * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
   */
  @Override
  public void installUI( final JComponent aComponent )
  {
    this.labelFont = LafHelper.getDefaultFont();
  }

  /**
   * @see javax.swing.plaf.ComponentUI#paint(java.awt.Graphics,
   *      javax.swing.JComponent)
   */
  @Override
  public void paint( final Graphics aCanvas, final JComponent aComponent )
  {
    final Graphics2D canvas = ( Graphics2D )aCanvas;

    final DiagramTimeLine timeLine = ( DiagramTimeLine )aComponent;

    final DataContainer dataContainer = timeLine.getDataContainer();
    if ( !dataContainer.hasCapturedData() )
    {
      return;
    }

    final Diagram diagram = timeLine.getDiagram();
    final DiagramSettings settings = diagram.getDiagramSettings();

    final double scale = timeLine.getScale();

    final int tickInc = Math.max( 1, ( int )( Diagram.MAX_SCALE / scale ) );
    final int timeLineShift = ( int )( dataContainer.getTriggerPosition() % tickInc );

    // obtain portion of graphics that needs to be drawn
    final Rectangle clipArea = canvas.getClipBounds();
    // for some reason, this component gets scrolled vertically although it has
    // no reasons to do so. Resetting the Y-position & height of the clip-area
    // seems to solve this problem...
    clipArea.y = 0;
    clipArea.height = TIMELINE_HEIGHT;

    // find index of first row that needs drawing
    final long firstRow = diagram.convertPointToSampleIndex( new Point( clipArea.x, 0 ) );
    // find index of last row that needs drawing
    final long lastRow = diagram.convertPointToSampleIndex( new Point( clipArea.x + clipArea.width, 0 ) ) + 1;

    canvas.setFont( this.labelFont );
    final FontMetrics fm = canvas.getFontMetrics();

    canvas.setColor( settings.getBackgroundColor() );
    canvas.fillRect( clipArea.x, clipArea.y, clipArea.width, clipArea.height );

    canvas.setColor( settings.getTimeColor() );

    for ( long time = ( firstRow / tickInc ) * tickInc + timeLineShift; time < lastRow; time += tickInc )
    {
      final int xPos = Math.max( 0, ( int )( scale * time ) );

      final int baselineYpos = clipArea.y + TIMELINE_HEIGHT - PADDING_Y;
      final int longTickYpos = ( int )( baselineYpos - ( 3.5 * SHORT_TICK_HEIGHT ) );
      final int shortTickYpos = baselineYpos - SHORT_TICK_HEIGHT;

      final long relativeTime = time - dataContainer.getTriggerPosition();
      final long scaledTime = relativeTime / tickInc;

      if ( scaledTime % LONG_TICK_INTERVAL == 0 )
      {
        final String timeValue = indexToTime( dataContainer, relativeTime );

        final int labelYpos = longTickYpos - 2 * PADDING_Y;
        final int labelXpos = Math.max( clipArea.x, xPos - ( fm.stringWidth( timeValue ) / 2 ) );

        canvas.drawLine( xPos, baselineYpos, xPos, longTickYpos );
        if ( scaledTime % TIME_INTERVAL == 0 )
        {
          canvas.drawString( timeValue, labelXpos, labelYpos );
        }
      }
      else
      {
        canvas.drawLine( xPos, baselineYpos, xPos, shortTickYpos );
      }
    }

    // If cursors are disabled entirely, we're done; otherwise we need to draw
    // them
    if ( !dataContainer.isCursorsEnabled() )
    {
      return;
    }

    final int textHeight = fm.getHeight();
    final int flagHeight = textHeight;

    for ( int i = 0, size = DataContainer.MAX_CURSORS; i < size; i++ )
    {
      final long cursorPosition = dataContainer.getCursorPosition( i );
      if ( ( cursorPosition >= firstRow ) && ( cursorPosition <= lastRow ) )
      {
        final int cursorPos = ( int )( cursorPosition * scale );

        final Color cursorColor = settings.getCursorColor( i );

        final String text = String.format( "T%d", i + 1 );

        final int textWidth = fm.stringWidth( text );
        final int flagWidth = textWidth + 4;

        canvas.setColor( cursorColor );
        canvas.fillRect( cursorPos, TIMELINE_HEIGHT - flagHeight, flagWidth, flagHeight );

        canvas.setColor( cursorColor.darker() );
        canvas.drawRect( cursorPos, TIMELINE_HEIGHT - flagHeight, flagWidth, flagHeight - 1 );

        canvas.setColor( cursorColor.darker().darker() );
        canvas.drawString( text, cursorPos + 3, TIMELINE_HEIGHT - 3 );
      }
    }
  }

  /**
   * Convert sample count to time string.
   * 
   * @param count
   *          sample count (or index)
   * @return string containing time information
   */
  private String indexToTime( final DataContainer aDataContainer, final long count )
  {
    if ( !aDataContainer.hasTimingData() )
    {
      return String.format( "%d", count );
    }
    return DisplayUtils.displayScaledTime( count, aDataContainer.getSampleRate() );
  }
}