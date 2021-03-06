/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectoseguridadservidor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Math.pow;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;
import static proyectoseguridadcliente.ProyectoSeguridadCliente.gDF;
import static proyectoseguridadcliente.ProyectoSeguridadCliente.nConstant;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *
 * @author carol
 */
public class ProyectoSeguridadServidor {

    private static final int PORT = 9001;
    private static HashMap<String, Usuario> usuarios;
    public static final int gDF = 2;
    public static final int nConstant = 761;

    /**
     * The set of all names of clients in the chat room. Maintained so that we
     * can check that new clients are not registering name already in use.
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * The set of all the print writers for all the clients. This set is kept so
     * we can easily broadcast messages.
     */
    private static HashMap<String, PrintWriter> writers = new HashMap<String, PrintWriter>();

    /**
     * The application main method, which just listens on a port and spawns
     * handler threads.
     */
    public static void main(String[] args) throws Exception {
        usuarios = new HashMap<>();
        leerArchivoUsuarios();
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();

        }
    }

    public static void leerArchivoUsuarios() {

        BufferedReader br = null;

        try {

            String sCurrentLine;
            String[] usuarioArray;

            br = new BufferedReader(new FileReader("usuarios.txt"));

            while ((sCurrentLine = br.readLine()) != null) {
                usuarioArray = sCurrentLine.split(",");
                usuarios.put(usuarioArray[0], new Usuario(usuarioArray[0], Integer.valueOf(usuarioArray[1]), usuarioArray[2], "Inactivo", "", ""));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    /**
     * A handler thread class. Handlers are spawned from the listening loop and
     * are responsible for a dealing with a single client and broadcasting its
     * messages.
     */
    private static class Handler extends Thread {

        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket. All the
         * interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;

        }

        /**
         * Services this thread's client by repeatedly requesting a screen name
         * until a unique one has been submitted, then acknowledges the name and
         * registers the output stream for the client in a global set, then
         * repeatedly gets inputs and broadcasts them.
         */
        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                int numeroA;
                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITNAME");
                    String ingreso = in.readLine();

                    if (ingreso == null) {
                        return;
                    } else {

                        String[] texto = ingreso.split(" ");
                        name = texto[0];
                        numeroA = Integer.valueOf(texto[1]);
                        System.out.println(texto[0] + " " + texto[1]);

                    }
                    if (usuarios.containsKey(name)) {
                        break;
                    } else {
                        out.println("REJECT");
                        socket.close();
                    }
                }

                int Puzzle_N = randIntPuzzle();

                out.println("SALTHASH" + " " + hashSaltNumber(usuarios.get(name).getSalt(), Puzzle_N) + " " + usuarios.get(name).getSalt());

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {

                    String ingreso = in.readLine();
                    if (ingreso == null) {
                        return;
                    } else {

                        String[] texto = ingreso.split(" ");
                        int N_obtenida = Integer.valueOf(texto[0]);

                        if (N_obtenida == Puzzle_N) {
                            System.out.println("N Correcto");
                            break;
                        } else {
                            out.println("REJECT");
                            socket.close();
                        }

                    }
                }
                SecureRandom Aleatorio_b = new SecureRandom();
                int b = (Math.abs(Aleatorio_b.nextInt()) % 100) + 1;
                System.out.println("aleatoria b: " + b);
                int numeroB = bResolver(usuarios.get(name).getPassword(), b);
                System.out.println("numero B: " + numeroB);
                int numeroU = uResolver(numeroA, numeroB);
                String hashUNString = hashMasUN(numeroU, Puzzle_N);
                out.println("BRESOLVER" + " " + numeroB + " " + hashUNString);
                System.out.println("numero U: " + numeroU);
                System.out.println("hashUNMAs: " + hashUNString);
                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {

                    String ingreso = in.readLine();
                    if (ingreso == null) {
                        return;
                    } else {

                        String[] texto = ingreso.split(" ");
                        String hashUObtenida = texto[0];
                        String hashUCreada = hashMenosUN(numeroU, Puzzle_N);

                        if (hashUCreada.equals(hashUObtenida)) {
                            System.out.println("hashes iguales");
                            break;
                        } else {
                            out.println("REJECT");
                            socket.close();
                        }

                    }

                    /* synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }*/
                }

                String claveServidor = llaveServidor(numeroA, usuarios.get(name).getPassword(), numeroU, b);
                System.out.println("clave Servidor: " + claveServidor);
                SecretKey llave_compartida = GenerarPass(claveServidor);

                while (true) {

                    String ingreso = in.readLine();
                    if (ingreso == null) {
                        return;
                    } else {
                        ingreso = new String(DecryptAES(ingreso, llave_compartida));

                        String[] texto = ingreso.split(" ");
                        String autenticado = texto[0];

                        if (autenticado.equals("AUTENTICADO")) {
                            System.out.println("Cliente Autenticado");
                            usuarios.get(name).setActivo("Activo");
                            usuarios.get(name).setClaveGeneradaServidor(claveServidor);
                            break;
                        } else {
                            out.println("REJECT");
                            socket.close();
                        }

                    }
                }

                writers.put(name, out);

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                String usuarioSeleccionado = "";

                while (true) {

                    String ingreso = in.readLine();

                    if (ingreso == null) {
                        return;
                    } else {
                        ingreso = new String(DecryptAES(ingreso, llave_compartida));
                        if (ingreso.startsWith("TALKTO")) {
                            String[] texto = ingreso.split(" ");
                            usuarioSeleccionado = texto[2];
                            if (usuarios.containsKey(usuarioSeleccionado)) {
                                System.out.println("Usuario Correcto");
                                for (HashMap.Entry<String, PrintWriter> entry : writers.entrySet()) {
                                    String key = entry.getKey();
                                    if (key.equals(usuarioSeleccionado)) {
                                        PrintWriter w = entry.getValue();
                                        String Valor = "CONEXION " + name;
                                        w.println(EncriptarAES(Valor.getBytes(), GenerarPass(usuarios.get(usuarioSeleccionado).getClaveGeneradaServidor())));
                                        System.out.println("Enviando conexion");
                                        break;

                                    }
                                }
                            } else {
                                out.println(EncriptarAES(("REJECT").getBytes(), llave_compartida));
                                usuarios.get(name).setActivo("");
                                usuarios.get(name).setClaveGeneradaServidor("");
                                socket.close();
                                break;
                            }

                        } else if (ingreso.startsWith("LISTA")) {

                            String userA = "";
                            for (Usuario value : usuarios.values()) {
                                if (value.getActivo().equals("Activo")) {
                                    userA += " " + value.getNombreUsuario();
                                }
                            }
                            out.println(EncriptarAES(("USUARIOSACTIVOS" + " " + userA).getBytes(), llave_compartida));

                        } else if (ingreso.startsWith("ACEPTO")) {
                            int randomNum = 3000 + (int) (Math.random() * 50000);
                            String[] sp = ingreso.split(" ");
                            System.out.println("USUARIO ACEPTADO");
                            String llaveClientes = "";
                            for (HashMap.Entry<String, PrintWriter> entry : writers.entrySet()) {
                                String key = entry.getKey();
                                if (key.equals(name)) {
                                    System.out.println(name + " " + sp[1]);
                                    String envio = "ENVIOLLAVE " + Calcular_Llave_Sesion_Cliente_Cliente(name, sp[1]);
                                    llaveClientes = EncriptarAES(envio.getBytes(), llave_compartida);
                                    PrintWriter w = entry.getValue();
                                    w.print(llaveClientes);
                                    w.print(EncriptarAES((" " + randomNum).getBytes(), llave_compartida));
                                    w.println(EncriptarAES((" " + socket.getRemoteSocketAddress().toString() + " " + "1").getBytes(), llave_compartida));
                                    System.out.println("Enviando clave " + name);

                                } else if (key.equals(sp[1])) {
                                    String envio = "ENVIOLLAVE " + Calcular_Llave_Sesion_Cliente_Cliente(name, sp[1]);
                                    llaveClientes = EncriptarAES(envio.getBytes(), GenerarPass(usuarios.get(sp[1]).getClaveGeneradaServidor()));
                                    PrintWriter w = entry.getValue();
                                    w.print(llaveClientes);
                                    w.print(EncriptarAES((" " + randomNum).getBytes(), GenerarPass(usuarios.get(sp[1]).getClaveGeneradaServidor())));
                                    w.println(EncriptarAES((" " + socket.getRemoteSocketAddress().toString() + " " + "2").getBytes(), GenerarPass(usuarios.get(sp[1]).getClaveGeneradaServidor())));
                                    System.out.println("Enviando clave " + sp[1]);

                                }

                            }
                        } else {
                            out.println(EncriptarAES(("REJECT").getBytes(), llave_compartida));
                            usuarios.get(name).setActivo("");
                            usuarios.get(name).setClaveGeneradaServidor("");
                            socket.close();
                            break;
                        }
                    }

                }

               /* out.println("NAMEACCEPTED");

                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    for (PrintWriter writer : writers.values()) {
                        writer.println("MESSAGE " + name + ": " + input);
                    }
                }*/
            } catch (IOException e) {
                System.out.println(e);
            } catch (Exception ex) {
                Logger.getLogger(ProyectoSeguridadServidor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        public String hashSaltNumber(int Salt, int Puzzle_N) {
            String Salt_Puzzle_N = String.valueOf(Salt) + String.valueOf(Puzzle_N);
            String sha1password = DigestUtils.sha256Hex(Salt_Puzzle_N);
            return sha1password;
        }

        public int randIntPuzzle() {
            int min = 1;
            int max = 256;
            Random rand = new Random();
            int randomNum = rand.nextInt((max - min) + 1) + min;
            return randomNum;
        }

        public Integer bResolver(String v, int b) {

            // int numeroV = hex2decimal(v);
            BigInteger numeroV = new BigInteger(v); //Integer.valueOf(v);
            numeroV = numeroV.multiply(new BigInteger(String.valueOf("3")));
            BigInteger numeroB = new BigInteger(String.valueOf(gDF));
            numeroB = numeroB.pow(b);
            numeroB = numeroB.add(numeroV);
            numeroB = numeroB.mod(new BigInteger(String.valueOf(nConstant)));

            //   numeroB = (int) (((numeroV) + (pow(gDF, b))) % nConstant);
            return numeroB.intValue();
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

        public String llaveServidor(Integer a, String v, int u, int b) {
            System.out.println("numero A: " + a);
            System.out.println("numero v: " + v);
            String llaveServidor;
            BigInteger numeroV = new BigInteger(v);

            numeroV = numeroV.pow(u);
            numeroV = numeroV.multiply(new BigInteger(String.valueOf(a)));
            numeroV = numeroV.pow(b);
            numeroV = numeroV.mod(new BigInteger(String.valueOf(nConstant)));
            System.out.println("resultado : " + numeroV);
            llaveServidor = DigestUtils.sha256Hex(String.valueOf(numeroV));

            return llaveServidor;
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

        public String Calcular_Llave_Sesion_Cliente_Cliente(String Nombre1, String Nombre2) {
            String Llave1 = usuarios.get(Nombre1).getClaveGeneradaServidor();
            String Llave2 = usuarios.get(Nombre2).getClaveGeneradaServidor();
            Timestamp tiempo = Timestamp.valueOf(LocalDateTime.MIN);
            String tiempo_string = tiempo.toString();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Llave1.length(); i++) {
                sb.append((char) (Llave1.charAt(i) ^ Llave2.charAt(i % Llave2.length())));
            }
            String result = sb.toString();
            String Llave_comp = tiempo_string + result;
            Llave_comp = DigestUtils.sha256Hex(Llave_comp);
            final int mid = Llave_comp.length() / 2;
            String[] parts = {Llave_comp.substring(0, mid), Llave_comp.substring(mid)};

            return parts[0];
        }
    }
}
