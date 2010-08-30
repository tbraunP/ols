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
package nl.lxtreme.ols.client.signal;


import java.awt.*;

import javax.swing.*;

import nl.lxtreme.ols.api.data.*;
import nl.lxtreme.ols.util.*;


/**
 * Provides a component that provides a timeline.
 */
public class DiagramTimeLine extends JComponent implements Scrollable, DiagramCursorChangeListener
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;
  /** The tick increment (in pixels). */
  public static final int TIMELINE_INCREMENT = 20;

  private static final int TIMELINE_HEIGHT = 30;
  private static final int SHORT_TICK_HEIGHT = 4;
  private static final int PADDING_Y = 1;

  // VARIABLES

  private double scale;
  private DiagramSettings diagramSettings;

  private final AnnotatedData annotatedData;

  // CONSTRUCTORS

  /**
   * Creates a new DiagramTimeLine instance.
   * 
   * @param aData
   *          the annotated data to use for this timeline.
   */
  public DiagramTimeLine( final AnnotatedData aData )
  {
    super();

    this.annotatedData = aData;

    setPreferredSize( new Dimension( TIMELINE_HEIGHT, TIMELINE_HEIGHT ) );
  }

  // METHODS

  /**
   * @see nl.lxtreme.ols.client.signal.DiagramCursorChangeListener#cursorChanged(int,
   *      int)
   */
  @Override
  public void cursorChanged( final int aCursorIdx, final int aMousePos )
  {
    repaint();
  }

  /**
   * @see nl.lxtreme.ols.client.signal.DiagramCursorChangeListener#cursorRemoved(int)
   */
  @Override
  public void cursorRemoved( final int aCursorIdx )
  {
    repaint();
  }

  /**
   * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
   */
  @Override
  public Dimension getPreferredScrollableViewportSize()
  {
    return getPreferredSize();
  }

  /**
   * Returns the scale.
   * 
   * @return the scale, never <code>null</code>.
   */
  public double getScale()
  {
    return this.scale;
  }

  /**
   * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle,
   *      int, int)
   */
  @Override
  public int getScrollableBlockIncrement( final Rectangle aVisibleRect, final int aOrientation, final int aDirection )
  {
    if ( aOrientation == SwingConstants.VERTICAL )
    {
      return 0;
    }

    return aVisibleRect.width - TIMELINE_INCREMENT;
  }

  /**
   * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
   */
  @Override
  public boolean getScrollableTracksViewportHeight()
  {
    return true;
  }

  /**
   * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
   */
  @Override
  public boolean getScrollableTracksViewportWidth()
  {
    return false;
  }

  /**
   * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle,
   *      int, int)
   */
  @Override
  public int getScrollableUnitIncrement( final Rectangle aVisibleRect, final int aOrientation, final int aDirection )
  {
    if ( aOrientation == SwingConstants.VERTICAL )
    {
      return 0;
    }

    int currentPosition = aVisibleRect.x;
    int maxUnitIncrement = DiagramTimeLine.TIMELINE_INCREMENT;

    // Return the number of pixels between currentPosition
    // and the nearest tick mark in the indicated direction.
    if ( aDirection < 0 )
    {
      int newPosition = currentPosition - ( currentPosition / maxUnitIncrement ) * maxUnitIncrement;
      return ( newPosition == 0 ) ? maxUnitIncrement : newPosition;
    }
    else
    {
      return ( ( currentPosition / maxUnitIncrement ) + 1 ) * maxUnitIncrement - currentPosition;
    }
  }

  /**
   * Sets the diagramSettings to the given value.
   * 
   * @param aDiagramSettings
   *          the diagramSettings to set, cannot be <code>null</code>.
   */
  public void setDiagramSettings( final DiagramSettings aDiagramSettings )
  {
    this.diagramSettings = aDiagramSettings;
  }

  /**
   * @see javax.swing.JComponent#setPreferredSize(java.awt.Dimension)
   */
  @Override
  public final void setPreferredSize( final Dimension aPreferredSize )
  {
    // Let us only scale in width, not height!
    super.setPreferredSize( new Dimension( aPreferredSize.width, TIMELINE_HEIGHT ) );
  }

  /**
   * Sets the scale to the given value.
   * 
   * @param aScale
   *          the scale to set, cannot be <code>null</code>.
   */
  public void setScale( final double aScale )
  {
    this.scale = aScale;
  }

  /**
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    if ( !this.annotatedData.hasCapturedData() )
    {
      return;
    }

    final int rowInc = Math.max( 1, ( int )( Diagram.MAX_SCALE / this.scale ) );
    final int timeLineShift = ( int )( this.annotatedData.getTriggerPosition() % rowInc );

    // obtain portion of graphics that needs to be drawn
    final Rectangle clipArea = aGraphics.getClipBounds();
    // for some reason, this component gets scrolled vertically although it has
    // no reasons to do so. Resetting the Y-position & height of the clip-area
    // seems to solve this problem...
    clipArea.y = 0;
    clipArea.height = TIMELINE_HEIGHT;

    // find index of first row that needs drawing
    final long firstRow = xToIndex( clipArea.x );
    // find index of last row that needs drawing
    final long lastRow = xToIndex( clipArea.x + clipArea.width ) + 1;

    final FontMetrics fm = aGraphics.getFontMetrics();

    aGraphics.setColor( this.diagramSettings.getBackgroundColor() );
    aGraphics.fillRect( clipArea.x, clipArea.y, clipArea.x + clipArea.width, clipArea.y + clipArea.height );

    aGraphics.setColor( this.diagramSettings.getTimeColor() );

    for ( long row = ( firstRow / rowInc ) * rowInc + timeLineShift; row < lastRow; row += rowInc )
    {
      final int pos = Math.max( 0, ( int )( this.scale * row ) );

      final int y1 = clipArea.y + TIMELINE_HEIGHT - PADDING_Y;
      final int y2 = y1 - 3 * SHORT_TICK_HEIGHT;
      final int y3 = y1 - SHORT_TICK_HEIGHT;

      final long relativeTime = row - this.annotatedData.getTriggerPosition();
      if ( ( relativeTime / rowInc ) % TIMELINE_INCREMENT == 0 )
      {
        final String time;
        if ( this.annotatedData.hasTimingData() )
        {
          time = indexToTime( relativeTime );
        }
        else
        {
          time = Long.toString( relativeTime );
        }

        final int labelYpos = y2 - 2 * PADDING_Y;
        int labelXpos = pos - ( fm.stringWidth( time ) / 2 );
        if ( labelXpos < clipArea.x )
        {
          labelXpos = clipArea.x;
        }

        aGraphics.drawLine( pos, y1, pos, y2 );
        aGraphics.drawString( time, labelXpos, labelYpos );
      }
      else
      {
        aGraphics.drawLine( pos, y1, pos, y3 );
      }
    }

    // draw cursor B first (lower priority)
    for ( int i = 0, size = AnnotatedData.MAX_CURSORS; i < size; i++ )
    {
      final long cursorPosition = this.annotatedData.getCursorPosition( i );
      if ( ( cursorPosition >= firstRow ) && ( cursorPosition <= lastRow ) )
      {
        final int cursorPos = ( int )( cursorPosition * this.scale );

        aGraphics.setColor( this.diagramSettings.getBackgroundColor() );
        aGraphics.fillRect( cursorPos, TIMELINE_HEIGHT - 14, 8, TIMELINE_HEIGHT - 1 );

        aGraphics.setColor( this.diagramSettings.getCursorColor( i ) );
        aGraphics.drawRect( cursorPos, TIMELINE_HEIGHT - 14, 8, TIMELINE_HEIGHT - 1 );
        aGraphics.drawString( Integer.toString( i + 1 ), cursorPos + 1, TIMELINE_HEIGHT - 2 );
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
  private String indexToTime( final long count )
  {
    if ( !this.annotatedData.hasTimingData() )
    {
      return String.format( "%d", count );
    }
    return DisplayUtils.displayScaledTime( count, this.annotatedData.getSampleRate() );
  }

  /**
   * Convert x position to sample index.
   * 
   * @param aXpos
   *          horizontal position in pixels
   * @return sample index
   */
  private long xToIndex( final int aXpos )
  {
    return Diagram.xToIndex( this.annotatedData, aXpos, this.scale );
  }
}

/* EOF */
