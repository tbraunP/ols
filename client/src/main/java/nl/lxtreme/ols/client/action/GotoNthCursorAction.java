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
 * 
 * Copyright (C) 2010-2011 - J.W. Janssen, http://www.lxtreme.nl
 */
package nl.lxtreme.ols.client.action;


import static nl.lxtreme.ols.client.icons.IconFactory.*;

import java.awt.event.*;

import javax.swing.*;

import nl.lxtreme.ols.api.*;
import nl.lxtreme.ols.api.data.*;
import nl.lxtreme.ols.client.actionmanager.*;
import nl.lxtreme.ols.client.icons.*;
import nl.lxtreme.ols.client.signaldisplay.*;
import nl.lxtreme.ols.util.*;
import nl.lxtreme.ols.util.swing.*;


/**
 * Generic action to go to a specific set cursor in the diagram.
 * 
 * @author stefant
 */
public class GotoNthCursorAction extends AbstractAction implements IManagedAction
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final String ID_PREFIX = "GotoCursor";

  // VARIABLES

  private final int index;
  private final SignalDiagramController controller;

  // METHODS

  /**
   * Creates a new GotoNthCursorAction instance.
   * 
   * @param aController
   *          the controller to use for this action;
   * @param aIndex
   *          the (zero-based) index of the cursor.
   */
  public GotoNthCursorAction( final SignalDiagramController aController, final int aIndex )
  {
    this.controller = aController;
    this.index = aIndex;

    final String cursorStr = String.valueOf( aIndex + 1 );

    putValue( NAME, "Go to cursor " + cursorStr );
    putValue( SHORT_DESCRIPTION, "Go to the " + DisplayUtils.getOrdinalNumber( aIndex + 1 ) + " cursor in the diagram" );
    putValue( Action.LARGE_ICON_KEY, createOverlayIcon( IconLocator.ICON_GOTO_CURSOR, cursorStr ) );

    int keyStroke = KeyEvent.VK_0 + ( ( aIndex + 1 ) % Ols.MAX_CURSORS );
    if ( keyStroke != KeyEvent.VK_0 )
    {
      // Avoid overwriting CTRL/CMD + 0 as accelerator...
      putValue( ACCELERATOR_KEY, SwingComponentUtils.createMenuKeyMask( keyStroke ) );
    }

    putValue( MNEMONIC_KEY, Integer.valueOf( keyStroke ) );
  }

  // METHODS

  /**
   * Creates an ID for the action to go the cursor with the given index.
   * 
   * @param aCursorIdx
   *          the cursor index to get the action-ID for, >= 0 && <
   *          {@value Ols#MAX_CURSORS}.
   * @return the action ID for the action, never <code>null</code>.
   */
  public static String getID( final int aCursorIdx )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx >= Ols.MAX_CURSORS ) )
    {
      throw new IllegalArgumentException( "Invalid cursor index, should be between 0 and " + Ols.MAX_CURSORS );
    }
    return ID_PREFIX + aCursorIdx;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed( final ActionEvent aEvent )
  {
    final Cursor[] definedCursors = this.controller.getDefinedCursors();
    for ( Cursor cursor : definedCursors )
    {
      if ( ( this.index == cursor.getIndex() ) && cursor.isDefined() )
      {
        this.controller.scrollToTimestamp( cursor.getTimestamp() );
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId()
  {
    return getID( this.index );
  }
}
