package com.ls.api.infs.printer.task.impl;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.ls.api.infs.printer.task.PrintTransportTask;
import com.ls.api.model.PrintTransport;
import com.ls.api.model.PrintType;

public class NetworkPrintTransportTask extends PrintTransportTask {

  /**
   * The default port for network printers.
   */
  public static final int DEFAULT_PRINTER_PORT = 9100;

  /**
   * The default port for mobile printers.
   */
  public static final int DEFAULT_MOBILE_PRINTER_PORT = 6101;

  /**
   * Denotes if the connection should be kept alive. This request is fire and
   * forget, it should not be maintained.
   */
  public static final boolean SOCKET_KEEP_ALIVE = false;

  public NetworkPrintTransportTask(PrintType type, PrintTransport transport, String target, String payload) {
    super(type, transport, target, payload);
  }

  @Override
  public boolean send() {
    try {

      final Socket socket = new Socket();
      socket.connect(new InetSocketAddress(target, DEFAULT_PRINTER_PORT));
      socket.setKeepAlive(SOCKET_KEEP_ALIVE);

      final OutputStream stream = new DataOutputStream(socket.getOutputStream());
      stream.write(payload.getBytes());

      /*
       * Forces any buffered bytes to be written out.
       */
      stream.flush();

      /*
       * Close the connection upon completion.
       */
      socket.close();

      /*
       * Denotes that the operation was successful on our end. This is not necessarily
       * indicative if the printer itself operated as intended post reception.
       */
      return true;

    } catch (Exception exception) {
      exception.printStackTrace(System.out);
    }
    return false;
  }

  @Override
  public boolean abort() {

    /*
     * Can not abort a fire and forget request.
     */
    return false;
  }
}