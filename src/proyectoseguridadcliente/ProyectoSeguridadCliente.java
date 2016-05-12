/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectoseguridadcliente;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Math.pow;
import java.math.BigInteger;
import java.net.Socket;
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

    /**
     * Constructs the client by laying out the GUI and registering a listener
     * with the textfield so that pressing Return in the listener sends the
     * textfield contents to the server. Note however that the textfield is
     * initially NOT editable, and only becomes editable AFTER the client
     * receives the NAMEACCEPTED message from the server.
     */
    public ProyectoSeguridadCliente() {

        // Layout GUI
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
    private void run() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, java.security.InvalidKeyException {

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
                frame.setTitle("Chat " + nombre);
                out.println(nombre + " " + numeroA); //TODO:Hay que modificar esta linea, poner el valor real de G y hacer elevado.
                pass = getPass();
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
                out.println(EncriptarAES("LISTA".getBytes(), w));
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("USUARIOSACTIVOS")) {
                getUsuariosActivos(line, nombre);
                clientesJCB.setEnabled(true);

            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            } else if (line.startsWith("REJECT")) {
                JOptionPane.showMessageDialog(
                        frame, "Conexion Rechazada!", "Screen reject", JOptionPane.ERROR_MESSAGE);
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            } else if (line.startsWith("CONEXION ")) {
                String[] us = line.split(" ");
                int dialogResult = JOptionPane.showConfirmDialog(null, "Desea conectarse con " + us[1], "Warning", JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    out.println(EncriptarAES("ACEPTO".getBytes(), w));
                    System.out.println("acepto");
                } else if (dialogResult == JOptionPane.NO_OPTION) {
                    out.println(EncriptarAES("NOACEPTO".getBytes(), w));
                    System.out.println("no acepto");
                }
            }
        }
    }

    public void getUsuariosActivos(String info, String usuario) {

        String[] usr = info.split(" ");

        for (int i = 2; i < usr.length; i++) {
            if (!usuario.equals(usr[i])) {
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

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ProyectoSeguridadCliente client = new ProyectoSeguridadCliente();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}
