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
package nl.lxtreme.ols.tool.uart;


import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import nl.lxtreme.ols.tool.base.*;
import nl.lxtreme.ols.util.*;
import nl.lxtreme.ols.util.swing.*;


/**
 * The Dialog Class
 * 
 * @author Frank Kunz The dialog class draws the basic dialog with a grid
 *         layout. The dialog consists of three main parts. A settings panel, a
 *         table panel and three buttons.
 */
public final class UARTProtocolAnalysisDialog extends BaseAsyncToolDialog<UARTDataSet, UARTAnalyserWorker>
{
  // INNER TYPES

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final String[] parityarray;
  private final String[] bitarray;
  private final String[] stoparray;
  private final JComboBox rxd;
  private final JComboBox txd;
  private final JComboBox cts;
  private final JComboBox rts;
  private final JComboBox dtr;
  private final JComboBox dsr;
  private final JComboBox dcd;
  private final JComboBox ri;
  private final JComboBox parity;
  private final JComboBox bits;
  private final JComboBox stop;
  private final JCheckBox inv;
  private final JEditorPane outText;

  private final RunAnalysisAction runAnalysisAction;
  private final ExportAction exportAction;
  private final CloseAction closeAction;

  // CONSTRUCTORS

  /**
   * @param aOwner
   * @param aName
   */
  public UARTProtocolAnalysisDialog( final Window aOwner, final String aName )
  {
    super( aOwner, aName );

    Container pane = getContentPane();
    pane.setLayout( new GridBagLayout() );
    getRootPane().setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

    /*
     * add protocol settings elements
     */
    JPanel panSettings = new JPanel();
    panSettings.setLayout( new GridLayout( 12, 2, 5, 5 ) );
    panSettings.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Settings" ),
        BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) ) );

    String channels[] = new String[33];
    for ( int i = 0; i < 32; i++ )
    {
      channels[i] = new String( "Channel " + i );
    }
    channels[channels.length - 1] = new String( "unused" );

    panSettings.add( new JLabel( "RxD" ) );
    this.rxd = new JComboBox( channels );
    panSettings.add( this.rxd );

    panSettings.add( new JLabel( "TxD" ) );
    this.txd = new JComboBox( channels );
    panSettings.add( this.txd );

    panSettings.add( new JLabel( "CTS" ) );
    this.cts = new JComboBox( channels );
    this.cts.setSelectedItem( "unused" );
    panSettings.add( this.cts );

    panSettings.add( new JLabel( "RTS" ) );
    this.rts = new JComboBox( channels );
    this.rts.setSelectedItem( "unused" );
    panSettings.add( this.rts );

    panSettings.add( new JLabel( "DTR" ) );
    this.dtr = new JComboBox( channels );
    this.dtr.setSelectedItem( "unused" );
    panSettings.add( this.dtr );

    panSettings.add( new JLabel( "DSR" ) );
    this.dsr = new JComboBox( channels );
    this.dsr.setSelectedItem( "unused" );
    panSettings.add( this.dsr );

    panSettings.add( new JLabel( "DCD" ) );
    this.dcd = new JComboBox( channels );
    this.dcd.setSelectedItem( "unused" );
    panSettings.add( this.dcd );

    panSettings.add( new JLabel( "RI" ) );
    this.ri = new JComboBox( channels );
    this.ri.setSelectedItem( "unused" );
    panSettings.add( this.ri );

    panSettings.add( new JLabel( "Parity" ) );
    this.parityarray = new String[3];
    this.parityarray[0] = new String( "none" );
    this.parityarray[1] = new String( "odd" );
    this.parityarray[2] = new String( "even" );
    this.parity = new JComboBox( this.parityarray );
    panSettings.add( this.parity );

    panSettings.add( new JLabel( "Bits" ) );
    this.bitarray = new String[4];
    for ( int i = 0; i < this.bitarray.length; i++ )
    {
      this.bitarray[i] = new String( "" + ( i + 5 ) );
    }
    this.bits = new JComboBox( this.bitarray );
    this.bits.setSelectedItem( "8" );
    panSettings.add( this.bits );

    panSettings.add( new JLabel( "Stopbit" ) );
    this.stoparray = new String[3];
    this.stoparray[0] = new String( "1" );
    this.stoparray[1] = new String( "1.5" );
    this.stoparray[2] = new String( "2" );
    this.stop = new JComboBox( this.stoparray );
    panSettings.add( this.stop );

    this.inv = new JCheckBox();
    panSettings.add( new JLabel( "Invert" ) );
    panSettings.add( this.inv );

    pane.add( panSettings, createConstraints( 0, 0, 1, 1, 0, 0 ) );

    /*
     * add an empty output view
     */
    JPanel panTable = new JPanel();
    panTable.setLayout( new GridLayout( 1, 1, 5, 5 ) );
    panTable.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Results" ),
        BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) ) );
    this.outText = new JEditorPane( "text/html", getEmptyHtmlPage() );
    this.outText.setMargin( new Insets( 5, 5, 5, 5 ) );
    panTable.add( new JScrollPane( this.outText ) );
    add( panTable, createConstraints( 1, 0, 3, 3, 1.0, 1.0 ) );

    /*
     * add buttons
     */
    this.runAnalysisAction = new RunAnalysisAction();
    JButton btnConvert = new JButton( this.runAnalysisAction );
    add( btnConvert, createConstraints( 0, 3, 1, 1, 1.0, 0 ) );

    this.exportAction = new ExportAction();
    JButton btnExport = new JButton( this.exportAction );
    add( btnExport, createConstraints( 1, 3, 1, 1, 1.0, 0 ) );

    this.closeAction = new CloseAction();
    JButton btnCancel = new JButton( this.closeAction );
    add( btnCancel, createConstraints( 2, 3, 1, 1, 1.0, 0 ) );

    setSize( 1000, 550 );
    setResizable( false );
  }

  // METHODS

  /**
   * @param x
   * @param y
   * @param w
   * @param h
   * @param wx
   * @param wy
   * @return
   */
  private static GridBagConstraints createConstraints( final int x, final int y, final int w, final int h,
      final double wx, final double wy )
  {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets( 4, 4, 4, 4 );
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = w;
    gbc.gridheight = h;
    gbc.weightx = wx;
    gbc.weighty = wy;
    return ( gbc );
  }

  /**
   * This is the UART protocol decoder core The decoder scans for a decode start
   * event like CS high to low edge or the trigger of the captured data. After
   * this the decoder starts to decode the data by the selected mode, number of
   * bits and bit order. The decoded data are put to a JTable object directly.
   */
  @Override
  public void onToolWorkerReady( final UARTDataSet aAnalysisResult )
  {
    super.onToolWorkerReady( aAnalysisResult );

    this.outText.setText( toHtmlPage( aAnalysisResult ) );
    this.outText.setEditable( false );

    this.exportAction.setEnabled( !aAnalysisResult.isEmpty() );
    this.runAnalysisAction.restore();
    this.runAnalysisAction.setEnabled( false );

    setControlsEnabled( true );
  }

  /**
   * @see nl.lxtreme.ols.api.Configurable#readProperties(java.lang.String,
   *      java.util.Properties)
   */
  public void readProperties( final String aNamespace, final Properties aProperties )
  {
    SwingComponentUtils.setSelectedItem( this.rxd, aProperties.getProperty( "tools.UARTProtocolAnalysis.rxd" ) );
    SwingComponentUtils.setSelectedItem( this.txd, aProperties.getProperty( "tools.UARTProtocolAnalysis.txd" ) );
    SwingComponentUtils.setSelectedItem( this.cts, aProperties.getProperty( "tools.UARTProtocolAnalysis.cts" ) );
    SwingComponentUtils.setSelectedItem( this.rts, aProperties.getProperty( "tools.UARTProtocolAnalysis.rts" ) );
    SwingComponentUtils.setSelectedItem( this.dtr, aProperties.getProperty( "tools.UARTProtocolAnalysis.dtr" ) );
    SwingComponentUtils.setSelectedItem( this.dsr, aProperties.getProperty( "tools.UARTProtocolAnalysis.dsr" ) );
    SwingComponentUtils.setSelectedItem( this.dcd, aProperties.getProperty( "tools.UARTProtocolAnalysis.dcd" ) );
    SwingComponentUtils.setSelectedItem( this.ri, aProperties.getProperty( "tools.UARTProtocolAnalysis.ri" ) );
    SwingComponentUtils.setSelectedItem( this.parity, aProperties.getProperty( "tools.UARTProtocolAnalysis.parity" ) );
    SwingComponentUtils.setSelectedItem( this.bits, aProperties.getProperty( "tools.UARTProtocolAnalysis.bits" ) );
    SwingComponentUtils.setSelectedItem( this.stop, aProperties.getProperty( "tools.UARTProtocolAnalysis.stop" ) );
    this.inv.setSelected( Boolean.parseBoolean( aProperties.getProperty( "tools.UARTProtocolAnalysis.inverted" ) ) );
  }

  /**
   * @see nl.lxtreme.ols.tool.base.ToolDialog#reset()
   */
  @Override
  public void reset()
  {
    this.outText.setText( getEmptyHtmlPage() );
    this.outText.setEditable( false );

    this.exportAction.setEnabled( false );

    this.runAnalysisAction.restore();
    this.runAnalysisAction.setEnabled( true );

    setControlsEnabled( true );
  }

  /**
   * @see nl.lxtreme.ols.api.Configurable#writeProperties(java.lang.String,
   *      java.util.Properties)
   */
  public void writeProperties( final String aNamespace, final Properties aProperties )
  {
    aProperties.setProperty( aNamespace + ".rxd", Integer.toString( this.rxd.getSelectedIndex() ) );
    aProperties.setProperty( aNamespace + ".txd", Integer.toString( this.txd.getSelectedIndex() ) );
    aProperties.setProperty( aNamespace + ".cts", Integer.toString( this.cts.getSelectedIndex() ) );
    aProperties.setProperty( aNamespace + ".rts", Integer.toString( this.rts.getSelectedIndex() ) );
    aProperties.setProperty( aNamespace + ".dtr", Integer.toString( this.dtr.getSelectedIndex() ) );
    aProperties.setProperty( aNamespace + ".dsr", Integer.toString( this.dsr.getSelectedIndex() ) );
    aProperties.setProperty( aNamespace + ".dcd", Integer.toString( this.dcd.getSelectedIndex() ) );
    aProperties.setProperty( aNamespace + ".ri", Integer.toString( this.ri.getSelectedIndex() ) );
    aProperties.setProperty( aNamespace + ".parity", ( String )this.parity.getSelectedItem() );
    aProperties.setProperty( aNamespace + ".bits", ( String )this.bits.getSelectedItem() );
    aProperties.setProperty( aNamespace + ".stop", ( String )this.stop.getSelectedItem() );
    aProperties.setProperty( aNamespace + ".inverted", "" + this.inv.isSelected() );
  }

  /**
   * set the controls of the dialog enabled/disabled
   * 
   * @param aEnable
   *          status of the controls
   */
  @Override
  protected void setControlsEnabled( final boolean aEnable )
  {
    this.rxd.setEnabled( aEnable );
    this.txd.setEnabled( aEnable );
    this.cts.setEnabled( aEnable );
    this.rts.setEnabled( aEnable );
    this.dtr.setEnabled( aEnable );
    this.dsr.setEnabled( aEnable );
    this.dcd.setEnabled( aEnable );
    this.ri.setEnabled( aEnable );
    this.parity.setEnabled( aEnable );
    this.bits.setEnabled( aEnable );
    this.stop.setEnabled( aEnable );
    this.inv.setEnabled( aEnable );

    this.exportAction.setEnabled( aEnable );
    this.closeAction.setEnabled( aEnable );
  }

  /**
   * @see nl.lxtreme.ols.tool.base.BaseAsyncToolDialog#setupToolWorker(nl.lxtreme.ols.tool.base.BaseAsyncToolWorker)
   */
  @Override
  protected void setupToolWorker( final UARTAnalyserWorker aToolWorker )
  {
    if ( !"unused".equals( this.rxd.getSelectedItem() ) )
    {
      aToolWorker.setRxdMask( 1 << this.rxd.getSelectedIndex() );
    }

    if ( !"unused".equals( this.txd.getSelectedItem() ) )
    {
      aToolWorker.setTxdMask( 1 << this.txd.getSelectedIndex() );
    }

    if ( !"unused".equals( this.cts.getSelectedItem() ) )
    {
      aToolWorker.setCtsMask( 1 << this.cts.getSelectedIndex() );
    }

    if ( !"unused".equals( this.rts.getSelectedItem() ) )
    {
      aToolWorker.setRtsMask( 1 << this.rts.getSelectedIndex() );
    }

    if ( !"unused".equals( this.dcd.getSelectedItem() ) )
    {
      aToolWorker.setDcdMask( 1 << this.dcd.getSelectedIndex() );
    }

    if ( !"unused".equals( this.ri.getSelectedItem() ) )
    {
      aToolWorker.setRiMask( 1 << this.ri.getSelectedIndex() );
    }

    if ( !"unused".equals( this.dsr.getSelectedItem() ) )
    {
      aToolWorker.setDsrMask( 1 << this.dsr.getSelectedIndex() );
    }

    if ( !"unused".equals( this.dtr.getSelectedItem() ) )
    {
      aToolWorker.setDtrMask( 1 << this.dtr.getSelectedIndex() );
    }

    // Other properties...
    aToolWorker.setInverted( this.inv.isSelected() );
    aToolWorker.setParity( UARTParity.parse( this.parity.getSelectedItem() ) );
    aToolWorker.setStopBits( UARTStopBits.parse( this.stop.getSelectedItem() ) );
  }

  /**
   * exports the data to a CSV file
   * 
   * @param aFile
   *          File object
   */
  @Override
  protected void storeToCsvFile( final File aFile, final UARTDataSet aDataSet )
  {
    if ( !aDataSet.isEmpty() )
    {
      UARTData dSet;

      try
      {
        BufferedWriter bw = new BufferedWriter( new FileWriter( aFile ) );

        bw.write( "\"" + "index" + "\",\"" + "time" + "\",\"" + "RxD data or event" + "\",\"" + "TxD data or event"
            + "\"" );
        bw.newLine();

        final List<UARTData> decodedData = aDataSet.getData();
        for ( int i = 0; i < decodedData.size(); i++ )
        {
          dSet = decodedData.get( i );
          switch ( dSet.getType() )
          {
            case UARTData.UART_TYPE_EVENT:
              bw.write( "\"" + i + "\",\"" + dSet.getTimeDisplayValue() + "\",\"" + dSet.getEvent() + "\",\""
                  + dSet.getEvent() + "\"" );
              break;

            case UARTData.UART_TYPE_RXEVENT:
              bw.write( "\"" + i + "\",\"" + dSet.getTimeDisplayValue() + "\",\"" + dSet.getEvent() + "\",\"" + "\"" );
              break;

            case UARTData.UART_TYPE_TXEVENT:
              bw.write( "\"" + i + "\",\"" + dSet.getTimeDisplayValue() + "\",\"" + "\",\"" + dSet.getEvent() + "\"" );
              break;

            case UARTData.UART_TYPE_RXDATA:
              bw.write( "\"" + i + "\",\"" + dSet.getTimeDisplayValue() + "\",\"" + dSet.getData() + "\",\"" + "\"" );
              break;

            case UARTData.UART_TYPE_TXDATA:
              bw.write( "\"" + i + "\",\"" + dSet.getTimeDisplayValue() + "\",\"" + "\",\"" + dSet.getData() + "\"" );
              break;

            default:
              break;
          }
          bw.newLine();
        }
        bw.close();
      }
      catch ( Exception E )
      {
        E.printStackTrace( System.out );
      }
    }
  }

  /**
   * stores the data to a HTML file
   * 
   * @param aFile
   *          file object
   */
  @Override
  protected void storeToHtmlFile( final File aFile, final UARTDataSet aDataSet )
  {
    if ( !aDataSet.isEmpty() )
    {
      try
      {
        BufferedWriter bw = new BufferedWriter( new FileWriter( aFile ) );

        // write the complete displayed html page to file
        bw.write( this.outText.getText() );

        bw.close();
      }
      catch ( Exception E )
      {
        E.printStackTrace( System.out );
      }
    }
  }

  /**
   * generate a HTML page
   * 
   * @param empty
   *          if this is true an empty output is generated
   * @return String with HTML data
   */
  private String getEmptyHtmlPage()
  {
    Date now = new Date();
    DateFormat df = DateFormat.getDateInstance( DateFormat.LONG, Locale.US );

    // generate html page header
    String header = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"
        + "<html>"
        + "  <head>"
        + "    <title></title>"
        + "    <meta content=\"\">"
        + "    <style>"
        + "           th { text-align:left;font-style:italic;font-weight:bold;font-size:medium;font-family:sans-serif;background-color:#C0C0FF; }"
        + "       </style>" + "  </head>" + "   <body>" + "       <H2>UART Analysis Results</H2>" + "       <hr>"
        + "           <div style=\"text-align:right;font-size:x-small;\">" + df.format( now ) + "           </div>"
        + "       <br>";

    // generate the statistics table
    String stats = new String();

    // generate the data table
    String data = "<table style=\"font-family:monospace;width:100%;\">"
        + "<tr><th style=\"width:15%;\">Index</th><th style=\"width:15%;\">Time</th><th style=\"width:10%;\">RxD Hex</th><th style=\"width:10%;\">RxD Bin</th><th style=\"width:8%;\">RxD Dec</th><th style=\"width:7%;\">RxD ASCII</th><th style=\"width:10%;\">TxD Hex</th><th style=\"width:10%;\">TxD Bin</th><th style=\"width:8%;\">TxD Dec</th><th style=\"width:7%;\">TxD ASCII</th></tr>";
    data = data.concat( "</table" );

    // generate the footer table
    String footer = "   </body>" + "</html>";

    return ( header + stats + data + footer );
  }

  /**
   * generate a HTML page
   * 
   * @param empty
   *          if this is true an empty output is generated
   * @return String with HTML data
   */
  private String toHtmlPage( final UARTDataSet aDataSet )
  {
    Date now = new Date();
    DateFormat df = DateFormat.getDateInstance( DateFormat.LONG );

    int bitCount = Integer.parseInt( ( String )this.bits.getSelectedItem() );
    int bitAdder = 0;

    if ( bitCount % 4 != 0 )
    {
      bitAdder = 1;
    }

    // generate html page header
    String header = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"
        + "<html>"
        + "  <head>"
        + "    <title></title>"
        + "    <meta content=\"\">"
        + "    <style>"
        + "           th { text-align:left;font-style:italic;font-weight:bold;font-size:medium;font-family:sans-serif;background-color:#C0C0FF; }"
        + "       </style>" + "  </head>" + "   <body>" + "       <H2>UART Analysis Results</H2>" + "       <hr>"
        + "           <div style=\"text-align:right;font-size:x-small;\">" + df.format( now ) + "           </div>"
        + "       <br>";

    // generate the statistics table
    String stats = new String();
    if ( aDataSet.getBitLength() == 0 )
    {
      stats = stats.concat( "<p style=\"color:red;\">Baudrate calculation failed !</p><br><br>" );
    }
    else
    {
      stats = stats.concat( "<table style=\"width:100%;\">" + "<TR><TD style=\"width:30%;\">Decoded Symbols</TD><TD>"
          + aDataSet.getDecodedSymbols() + "</TD></TR>" + "<TR><TD style=\"width:30%;\">Detected Bus Errors</TD><TD>"
          + aDataSet.getDetectedErrors() + "</TD></TR>" + "<TR><TD style=\"width:30%;\">Baudrate</TD><TD>"
          + aDataSet.getSampleRate() / aDataSet.getBitLength() + "</TD></TR>" + "</table>" + "<br>" + "<br>" );
      if ( aDataSet.getBitLength() < 15 )
      {
        stats = stats
            .concat( "<p style=\"color:red;\">The baudrate may be wrong, use a higher samplerate to avoid this !</p><br><br>" );
      }
    }

    // generate the data table
    String data = "<table style=\"font-family:monospace;width:100%;\">"
        + "<tr><th style=\"width:15%;\">Index</th><th style=\"width:15%;\">Time</th><th style=\"width:10%;\">RxD Hex</th><th style=\"width:10%;\">RxD Bin</th><th style=\"width:8%;\">RxD Dec</th><th style=\"width:7%;\">RxD ASCII</th><th style=\"width:10%;\">TxD Hex</th><th style=\"width:10%;\">TxD Bin</th><th style=\"width:8%;\">TxD Dec</th><th style=\"width:7%;\">TxD ASCII</th></tr>";
    final List<UARTData> decodedData = aDataSet.getData();

    UARTData ds;
    for ( int i = 0; i < decodedData.size(); i++ )
    {
      ds = decodedData.get( i );
      switch ( ds.getType() )
      {
        case UARTData.UART_TYPE_EVENT:
          data = data.concat( "<tr style=\"background-color:#E0E0E0;\"><td>" + i + "</td><td>"
              + ds.getTimeDisplayValue() + "</td><td>" + ds.getEvent() + "</td><td></td><td></td><td></td><td>"
              + ds.getEvent() + "</td><td></td><td></td><td></td></tr>" );
          break;

        case UARTData.UART_TYPE_RXEVENT:
          data = data.concat( "<tr style=\"background-color:#E0E0E0;\"><td>" + i + "</td><td>"
              + ds.getTimeDisplayValue() + "</td><td>" + ds.getEvent() + "</td><td></td><td></td><td></td><td>"
              + "</td><td></td><td></td><td></td></tr>" );
          break;

        case UARTData.UART_TYPE_TXEVENT:
          data = data.concat( "<tr style=\"background-color:#E0E0E0;\"><td>" + i + "</td><td>"
              + ds.getTimeDisplayValue() + "</td><td>" + "</td><td></td><td></td><td></td><td>" + ds.getEvent()
              + "</td><td></td><td></td><td></td></tr>" );
          break;

        case UARTData.UART_TYPE_RXDATA:
          final int rxdData = ds.getData();
          data = data.concat( "<tr style=\"background-color:#FFFFFF;\"><td>" + i + "</td><td>"
              + ds.getTimeDisplayValue() + "</td><td>" + "0x"
              + DisplayUtils.integerToHexString( rxdData, bitCount / 4 + bitAdder ) + "</td><td>" + "0b"
              + DisplayUtils.integerToBinString( rxdData, bitCount ) + "</td><td>" + rxdData + "</td><td>" );

          if ( ( rxdData >= 32 ) && ( bitCount == 8 ) )
          {
            data += ( char )rxdData;
          }
          data = data.concat( "</td><td>" + "</td><td>" + "</td><td>" + "</td><td>" );
          data = data.concat( "</td></tr>" );
          break;

        case UARTData.UART_TYPE_TXDATA:
          final int txdData = ds.getData();
          data = data.concat( "<tr style=\"background-color:#FFFFFF;\"><td>" + i + "</td><td>"
              + ds.getTimeDisplayValue() + "</td><td>" + "</td><td>" + "</td><td>" + "</td><td>" );

          data = data.concat( "</td><td>" + "0x" + DisplayUtils.integerToHexString( txdData, bitCount / 4 + bitAdder )
              + "</td><td>" + "0b" + DisplayUtils.integerToBinString( txdData, bitCount ) + "</td><td>" + txdData
              + "</td><td>" );

          if ( ( txdData >= 32 ) && ( bitCount == 8 ) )
          {
            data += ( char )txdData;
          }
          data = data.concat( "</td></tr>" );
          break;

        default:
          break;
      }

    }
    data = data.concat( "</table" );

    // generate the footer table
    String footer = "   </body>" + "</html>";

    return ( header + stats + data + footer );
  }
}
