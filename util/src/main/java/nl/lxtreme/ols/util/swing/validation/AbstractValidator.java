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
package nl.lxtreme.ols.util.swing.validation;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import nl.lxtreme.ols.util.swing.*;


/**
 * @author jawi
 */
public abstract class AbstractValidator extends InputVerifier implements KeyListener
{
  // CONSTANTS

  private static final String DEFAULT_MESSAGE = "Input invalid!";

  private static final byte[] ERROR_GIF_BYTES = { 71, 73, 70, 56, 57, 97, 16, 0, 16, 0, -77, 0, 0, -1, 127, 63, -8, 88,
      56, -1, 95, 63, -8, 56, 56, -33, 63, 63, -65, 63, 63, -104, 56, 56, 127, 63, 63, -1, -65, -65, -97, 127, 127, -1,
      -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 33, -7, 4, 1, 0, 0, 11, 0, 44, 0, 0, 0, 0, 16, 0, 16, 0,
      0, 4, 84, 112, -55, 73, -85, -67, 120, -91, -62, 75, -54, -59, 32, 14, 68, 97, 92, 33, -96, 8, 65, -96, -104, 85,
      50, 0, 0, -94, 12, 10, 82, 126, 83, 26, -32, 57, 18, -84, 55, 96, 1, 69, -91, 3, 37, -12, -77, -35, -124, 74, 98,
      -64, 54, -96, -106, 78, -109, 4, 1, 55, 66, 32, 76, -68, -119, -127, 64, 46, -101, -94, 21, 67, -121, 99, 64, 91,
      18, -19, -125, 33, -100, -87, -37, 41, 17, 0, 59 };
  private static final ImageIcon ERROR_ICON = new ImageIcon( ERROR_GIF_BYTES );

  // VARIABLES

  private final String message;

  private JDialog popup;

  // CONSTRUCTORS

  /**
   * Creates a new AbstractValidator instance.
   * 
   * @param aMessage
   *          the message to use when validation fails.
   */
  public AbstractValidator( final String aMessage )
  {
    super();

    this.message = aMessage;
  }

  // METHODS

  /**
   * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
   */
  @Override
  public final void keyPressed( final KeyEvent aEvent )
  {
    final Component source = ( Component )aEvent.getSource();
    source.removeKeyListener( this );

    if ( this.popup != null )
    {
      this.popup.setVisible( false );
      this.popup.dispose();
      this.popup = null;
    }
  }

  /**
   * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  @Override
  public final void keyReleased( final KeyEvent aEvent )
  {
    // NO-op
  }

  /**
   * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  @Override
  public final void keyTyped( final KeyEvent aEvent )
  {
    // NO-op
  }

  /**
   * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
   */
  @Override
  public final boolean verify( final JComponent aInput )
  {
    boolean result = doVerify( aInput );
    if ( !result )
    {
      if ( ( this.popup != null ) && ( this.popup.getOwner() != SwingComponentUtils.getOwningWindow( aInput ) ) )
      {
        this.popup.setVisible( false );
        this.popup.dispose();
        this.popup = null;
      }
      if ( this.popup == null )
      {
        this.popup = createMessagePopup( aInput );
      }

      // Allow the popup to be dismissed when a key is pressed...
      aInput.addKeyListener( this );

      // Signal that the input is incorrect...
      aInput.setBackground( Color.PINK );

      this.popup.setSize( 0, 0 );
      this.popup.setLocationRelativeTo( aInput );

      final Point point = this.popup.getLocation();
      final Dimension componentSize = aInput.getSize();

      this.popup.setLocation( point.x - ( int )componentSize.getWidth() / 2, point.y + ( int )componentSize.getHeight()
          / 2 );

      this.popup.pack();
      this.popup.setVisible( true );
    }
    else
    {
      aInput.setBackground( Color.WHITE );
    }

    return result;
  }

  /**
   * Implement this method to perform the actual verification of the given
   * component.
   * 
   * @param aInput
   *          the component to verify, cannot be <code>null</code>.
   * @return <code>true</code> if the component is correct, <code>false</code>
   *         otherwise.
   */
  protected abstract boolean doVerify( final JComponent aInput );

  /**
   * Returns the message to use when validation fails.
   * 
   * @return a message, might be <code>null</code>.
   */
  protected String getMessage()
  {
    return this.message;
  }

  /**
   * Creates a new message popup.
   * 
   * @return the error message popup, never <code>null</code>.
   */
  private JDialog createMessagePopup( final JComponent aComponent )
  {
    String message = getMessage();
    if ( ( message == null ) || message.trim().isEmpty() )
    {
      message = DEFAULT_MESSAGE;
    }

    final JDialog result = new JDialog( SwingComponentUtils.getOwningWindow( aComponent ) );
    result.setFocusableWindowState( false );
    result.setUndecorated( true );

    final Container contentPane = result.getContentPane();
    contentPane.setLayout( new FlowLayout() );
    contentPane.add( new JLabel( ERROR_ICON ) );
    contentPane.add( new JLabel( message ) );

    return result;
  }

}
