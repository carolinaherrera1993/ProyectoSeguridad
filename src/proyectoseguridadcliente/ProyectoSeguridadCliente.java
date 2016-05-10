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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.commons.codec.digest.DigestUtils;

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
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.pack();

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
    private void run() throws IOException {

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
        // Process all messages from server, according to the protocol.
        while (true) {
            String line = in.readLine();
            if (line == null) {
                return;
            } else if (line.startsWith("SUBMITNAME")) {
                numeroA = (int) ((pow(gDF, a)) % nConstant);
                out.println(getName() + " " + numeroA); //TODO:Hay que modificar esta linea, poner el valor real de G y hacer elevado.
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
                out.println("AUTENTICADO");
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("USUARIOSACTIVOS")) {
                getUsuariosActivos(line);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            } else if (line.startsWith("REJECT")) {
                JOptionPane.showMessageDialog(
                        frame, "Conexion Rechazada!", "Screen reject", JOptionPane.ERROR_MESSAGE);
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        }
    }

    public void getUsuariosActivos(String info) {

        String[] usr = info.split(" ");

        for (int i = 1; i < usr.length; i++) {
            usuariosActivos.add(usr[i]);
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
        System.out.println("x: " +x);
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
