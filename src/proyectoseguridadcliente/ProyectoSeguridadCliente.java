/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectoseguridadcliente;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import static java.lang.Math.pow;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.management.openmbean.InvalidKeyException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.commons.codec.digest.DigestUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *
 * @author carol
 */
public class ProyectoSeguridadCliente {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    public static final int gDF = 2;
    public static final int nConstant = 761;
    ArrayList<String> usuariosActivos;
    JComboBox clientesJCB;
    DefaultComboBoxModel usuariosClientes = new DefaultComboBoxModel();
    String clienteSeleccionado;
    String nombre = "";
    SecretKey w = null;
    JButton listaButton;

    /**
     * Constructs the client by laying out the GUI and registering a listener
     * with the textfield so that pressing Return in the listener sends the
     * textfield contents to the server. Note however that the textfield is
     * initially NOT editable, and only becomes editable AFTER the client
     * receives the NAMEACCEPTED message from the server.
     */
    public ProyectoSeguridadCliente() {

        // Layout GUI
        listaButton = new JButton("Lista");
        listaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String info = "LISTA";
                try {
                    out.println(EncriptarAES(info.getBytes(), w));
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(ProyectoSeguridadCliente.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchPaddingException ex) {
                    Logger.getLogger(ProyectoSeguridadCliente.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidKeyException ex) {
                    Logger.getLogger(ProyectoSeguridadCliente.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalBlockSizeException ex) {
                    Logger.getLogger(ProyectoSeguridadCliente.class.getName()).log(Level.SEVERE, null, ex);
                } catch (BadPaddingException ex) {
                    Logger.getLogger(ProyectoSeguridadCliente.class.getName()).log(Level.SEVERE, null, ex);
                } catch (java.security.InvalidKeyException ex) {
                    Logger.getLogger(ProyectoSeguridadCliente.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        listaButton.setEnabled(false);
        usuariosActivos = new ArrayList<>();
        usuariosClientes.addElement("Seleccione el Cliente:");
        textField.setText("Ingrese el texto para enviar ");
        clientesJCB = new JComboBox(usuariosClientes);
        textField.setEditable(false);
        messageArea.setEditable(false);
        clientesJCB.setEnabled(false);
        frame.getContentPane().add(textField, "South");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.getContentPane().add(clientesJCB, "North");
        frame.getContentPane().add(listaButton, "East");
        clientesJCB.setSelectedIndex(0);
        frame.pack();

        clientesJCB.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    JComboBox<String> combo = (JComboBox<String>) event.getSource();
                    clienteSeleccionado = (String) combo.getSelectedItem();

                    System.out.println("Cliente seleccionado: " + clienteSeleccionado);
                    String info = "TALKTO" + " " + nombre + " " + clienteSeleccionado;
                    out.println(EncriptarAES(info.getBytes(), w));
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(ProyectoSeguridadCliente.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchPaddingException ex) {
                    Logger.getLogger(ProyectoSeguridadCliente.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidKeyException ex) {
                    Logger.getLogger(ProyectoSeguridadCliente.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalBlockSizeException ex) {
                    Logger.getLogger(ProyectoSeguridadCliente.class.getName()).log(Level.SEVERE, null, ex);
                } catch (BadPaddingException ex) {
                    Logger.getLogger(ProyectoSeguridadCliente.class.getName()).log(Level.SEVERE, null, ex);
                } catch (java.security.InvalidKeyException ex) {
                    Logger.getLogger(ProyectoSeguridadCliente.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        // Add Listeners
        textField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server. Then clear the text
             * area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }

    public int randInt() {

        int min = 1;
        int max = 256;
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    /**
     * Prompt for and return the address of the server.
     */
    /* private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }*/
    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
        return JOptionPane.showInputDialog(
                frame, "Ingrese el nombre de usuario:", "Screen name selection", JOptionPane.PLAIN_MESSAGE);
    }

    private String getPass() {
        return JOptionPane.showInputDialog(
                frame, "Ingrese la contraseña:", "Screen name selection", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, java.security.InvalidKeyException, InterruptedException {

        SecureRandom Aleatorio_a = new SecureRandom();

        // Make connection and initialize streams
        String serverAddress = "localhost";
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        int numeroA = 0;
        int a = (Math.abs(Aleatorio_a.nextInt()) % 100) + 1;
        System.out.println("aleatoria a: " + a);
        int numeroB;
        int numeroU;
        int numeroN = 0;
        int salt = 0;
        String pass = "";
        //w = null;
        // Process all messages from server, according to the protocol.
        while (true) {
            String line = in.readLine();
            if (line == null) {
                return;
            } else if (line.startsWith("SUBMITNAME")) {
                numeroA = (int) ((pow(gDF, a)) % nConstant);
                nombre = getName();
                if (nombre == null || nombre.equals("")) {
                    JOptionPane.showMessageDialog(
                            frame, "Conexion Rechazada! No ingreso el usuario", "Screen reject", JOptionPane.ERROR_MESSAGE);
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                    break;
                }
                frame.setTitle("Chat " + nombre);
                out.println(nombre + " " + numeroA); //TODO:Hay que modificar esta linea, poner el valor real de G y hacer elevado.
                pass = getPass();
                if (pass == null || pass.equals("")) {
                    JOptionPane.showMessageDialog(
                            frame, "Conexion Rechazada! No ingreso la contrasena", "Screen reject", JOptionPane.ERROR_MESSAGE);
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                    break;
                }
            } else if (line.startsWith("SALTHASH")) {
                String[] separarHash = line.split(" ");
                salt = Integer.valueOf(separarHash[2]);
                numeroN = restoreSaltHash(String.valueOf(salt), separarHash[1]);
                out.println(numeroN);
                System.out.println(numeroN);
            } else if (line.startsWith("BRESOLVER")) {
                String[] ing = line.split(" ");
                numeroB = Integer.valueOf(ing[1]);
                System.out.println("numero B: " + numeroB);
                numeroU = uResolver(numeroA, numeroB);
                System.out.println("numero U: " + numeroU);
                String hashNumeroUMenos = hashMenosUN(numeroU, numeroN);
                String hashNumeroUMas = hashMasUN(numeroU, numeroN);
                System.out.println("hashUNMAS: " + hashNumeroUMas);
                if (!hashNumeroUMas.equals(ing[2])) {
                    JOptionPane.showMessageDialog(
                            frame, "Conexion Rechazada!", "Screen reject", JOptionPane.ERROR_MESSAGE);
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                }
                out.println(hashNumeroUMenos);
                int x = Calcular_X(salt, pass);
                String compartido = Generar_Secreto(numeroB, x, a, numeroU);
                System.out.println("clave cliente: " + compartido);
                w = GenerarPass(compartido);
                out.println(EncriptarAES("AUTENTICADO".getBytes(), w));
                //out.println(EncriptarAES("LISTA".getBytes(), w));                
                listaButton.setEnabled(true);
                break;
            } else if (line.startsWith("REJECT")) {
                JOptionPane.showMessageDialog(
                        frame, "Conexion Rechazada!", "Screen reject", JOptionPane.ERROR_MESSAGE);
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                break;
            }
        }

        while (true) {
            String line = in.readLine();
            if (line == null) {
                return;
            } else {
                line = new String(DecryptAES(line, w));
                if (line.startsWith("NAMEACCEPTED")) {
                    textField.setEditable(true);
                } else if (line.startsWith("USUARIOSACTIVOS")) {
                    getUsuariosActivos(line, nombre);
                    clientesJCB.setEnabled(true);
                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n");
                } else if (line.startsWith("CONEXION")) {
                    String[] us = line.split(" ");
                    int dialogResult = JOptionPane.showConfirmDialog(null, "Desea conectarse con " + us[1], "Warning", JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        String acp = "ACEPTO " + us[1];
                        out.println(EncriptarAES(acp.getBytes(), w));
                        System.out.println("acepto");
                    } else if (dialogResult == JOptionPane.NO_OPTION) {
                        out.println(EncriptarAES("NOACEPTO".getBytes(), w));
                        System.out.println("no acepto");
                    }
                } else if (line.startsWith("ENVIOLLAVE")) {
                    String[] us = line.split(" ");
                    String Llave_cliente = us[1];
                    String p = us[2].replaceAll("\n", "");
                    int Puerto = Integer.valueOf(p);
                    String IP_Remoto = "127.0.0.1";
                    int Flag = Integer.valueOf(us[4]);
                    if (Flag == 1) {
                        Crear_socket(Puerto);
                    } else {
                        Thread.sleep(500);
                        Unirse_socket(IP_Remoto, Puerto);

                    }
                } else if (line.startsWith("REJECT")) {
                    JOptionPane.showMessageDialog(
                            frame, "Conexion Rechazada!", "Screen reject", JOptionPane.ERROR_MESSAGE);
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                }

            }

        }
    }

    public void getUsuariosActivos(String info, String usuario) {

        String[] usr = info.split(" ");

        for (int i = 2; i < usr.length; i++) {
            if (usuario.equals(usr[i])) {

            } else if (!usuariosActivos.contains(usr[i])) {
                usuariosActivos.add(usr[i]);
                usuariosClientes.addElement(usr[i]);
            }

        }

    }

    public Integer restoreSaltHash(String salt, String hash) {

        Integer num = 1;

        for (int x = 0; x < 256; x++) {
            String intento = salt + String.valueOf(x);
            if (hash.contentEquals(DigestUtils.sha256Hex(intento))) {
                return x;
            }

        }

        return -1;
    }

    public Integer uResolver(int a, int b) {

        Integer numeroU = 0;

        int u = a + b;
        String sha1password = DigestUtils.sha256Hex(String.valueOf(u));

        numeroU = Math.abs(hex2decimal(sha1password));
        numeroU = numeroU % 100;

        return numeroU;
    }

    public int hex2decimal(String s) {

        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16 * val + d;
        }
        return val;
    }

    public String hashMasUN(int u, int n) {

        int numeroUN = u + n;
        String sha1password = DigestUtils.sha256Hex(String.valueOf(numeroUN));
        return sha1password;

    }

    public String hashMenosUN(int u, int n) {

        int numeroUN = u - n;
        String sha1password = DigestUtils.sha256Hex(String.valueOf(numeroUN));
        return sha1password;

    }

    public int Calcular_X(int sal, String pass) {
        String salt = String.valueOf(sal);
        String Hash = DigestUtils.sha256Hex(salt + pass);
        int x = Math.abs(hex2decimal(Hash) % 1000);
        System.out.println("x: " + x);
        return x;
    }

    public String Generar_Secreto(int B, int x, int a, int u) {
        BigInteger primero = new BigInteger(String.valueOf(gDF));
        primero = primero.pow(x);
        primero = primero.multiply(new BigInteger("3"));
        BigInteger nominador = new BigInteger(String.valueOf(B));
        primero = nominador.subtract(primero);
        int res_par = a + (u * x);
        BigInteger result = primero.pow(res_par);
        result = result.mod(new BigInteger(String.valueOf(nConstant)));
        System.out.println("resultado: " + result);
        return DigestUtils.sha256Hex(result.toString());
    }

    public String EncriptarAES(byte[] texto, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, javax.management.openmbean.InvalidKeyException, IllegalBlockSizeException, BadPaddingException, java.security.InvalidKeyException {

        Cipher AesCipher = Cipher.getInstance("AES");
        AesCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] byteCipherText = AesCipher.doFinal(texto);
        String Texto = new BASE64Encoder().encode(byteCipherText);
        return Texto;
    }

    public byte[] DecryptAES(String cipherText, SecretKey key) throws NoSuchAlgorithmException, javax.management.openmbean.InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, java.security.InvalidKeyException, IOException {
        byte[] Texto = new BASE64Decoder().decodeBuffer(cipherText);
        Cipher AesCipher = Cipher.getInstance("AES");
        AesCipher.init(Cipher.DECRYPT_MODE, key);
        byte[] bytePlainText = AesCipher.doFinal(Texto);
        return bytePlainText;
    }

    public SecretKey GenerarPass(String Hash) throws NoSuchAlgorithmException {
        byte[] key = Hash.getBytes();
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16); // use only first 128 bit
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

    public void Crear_socket(int port) {

        ChatServer chat = new ChatServer(port);

    }

    public void Unirse_socket(String server, int port) {

        ChatClient chat = new ChatClient(server, port);

    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ProyectoSeguridadCliente client = new ProyectoSeguridadCliente();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }

    public void conectarSocket(String server, int puerto) {

        String serverName = server;
        int port = puerto;
        try {
            System.out.println("Connecting to " + serverName
                    + " on port " + port);
            Socket client = new Socket(serverName, port);
            System.out.println("Just connected to "
                    + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF("Hello from "
                    + client.getLocalSocketAddress());
            InputStream inFromServer = client.getInputStream();
            DataInputStream in
                    = new DataInputStream(inFromServer);
            System.out.println("Server says " + in.readUTF());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public class ChatServer {

        private Socket socket = null;
        private ServerSocket server = null;
        private DataInputStream streamIn = null;
        private DataInputStream console = null;
        private DataOutputStream streamOut = null;

        public ChatServer(int port) {
            try {
                System.out.println("Binding to port " + port + ", please wait  ...");
                server = new ServerSocket(port);
                System.out.println("Server started: " + server);
                System.out.println("Waiting for a client ...");
                socket = server.accept();
                System.out.println("Client accepted: " + socket);
                open();
                boolean done = false;
                String line = "";
                // start();
                while (!done) {
                    try {
                        line = streamIn.readUTF();
                        System.out.println(line);
                        done = line.equals(".bye");
                    } catch (IOException ioe) {
                        done = true;
                    }
                }
                close();
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }

        public void open() throws IOException {
            streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            streamOut = new DataOutputStream(socket.getOutputStream());
        }

        public void close() throws IOException {
            if (socket != null) {
                socket.close();
            }
            if (streamIn != null) {
                streamIn.close();
            }
        }

    }

    public class ChatClient {

        private Socket socket = null;
        private DataInputStream console = null;
        private DataOutputStream streamOut = null;
        private DataInputStream streamIn = null;

        public ChatClient(String serverName, int serverPort) {
            System.out.println("Establishing connection. Please wait ...");
            try {
                socket = new Socket(serverName, serverPort);
                System.out.println("Connected: " + socket);
                start();
            } catch (UnknownHostException uhe) {
                System.out.println("Host unknown: " + uhe.getMessage());
            } catch (IOException ioe) {
                System.out.println("Unexpected exception: " + ioe.getMessage());
            }
            String line = "";
            while (!line.equals(".bye")) {
                try {
                    line = console.readLine();
                    streamOut.writeUTF(line);
                    streamOut.flush();
                } catch (IOException ioe) {
                    System.out.println("Sending error: " + ioe.getMessage());
                }
            }
        }

        public void start() throws IOException {
            console = new DataInputStream(System.in);
            streamOut = new DataOutputStream(socket.getOutputStream());
            streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        }

        public void stop() {
            try {
                if (console != null) {
                    console.close();
                }
                if (streamOut != null) {
                    streamOut.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error closing ...");
            }
        }

    }

}
