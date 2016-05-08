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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author carol
 */
public class ProyectoSeguridadServidor {

    private static final int PORT = 9001;
    private static HashMap<String, Usuario> usuarios;
    public static final int gDF = 2;
    public static final int nConstant = 53;
    /**
     * The set of all names of clients in the chat room. Maintained so that we
     * can check that new clients are not registering name already in use.
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * The set of all the print writers for all the clients. This set is kept so
     * we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    /**
     * The appplication main method, which just listens on a port and spawns
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
                usuarios.put(usuarioArray[0], new Usuario(usuarioArray[0], Integer.valueOf(usuarioArray[1]), usuarioArray[2]));

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

                int numeroB = bResolver(Integer.valueOf(usuarios.get(name).getPassword()));
                int numeroU = uResolver(numeroA, numeroB);
                String hashUNString=hashMasUN(numeroU);
                out.println("BRESOLVER" + " " + numeroB + " " + hashUNString);
                System.out.println(numeroU);

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {

                    String ingreso = in.readLine();
                    if (ingreso == null) {
                        return;
                    } else {

                        String[] texto = ingreso.split(" ");
                        String hashUObtenida = texto[0];
                        String hashUCreada = hashMenosUN(numeroU);

                        if (hashUCreada.equals(hashUObtenida)) {
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

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");
                writers.add(out);

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + ": " + input);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
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

        public Integer bResolver(Integer v) {

            Integer numeroB = 0;
            numeroB = (3 * (v) + (gDF ^ (randIntPuzzle())) % nConstant);
            return numeroB;
        }

        public Integer uResolver(int a, int b) {

            Integer numeroU = 0;

            int u = a + b;
            String sha1password = DigestUtils.sha256Hex(String.valueOf(u));

            numeroU = hex2decimal(sha1password);
            numeroU = numeroU % 3;

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

        public String hashMasUN(int u) {
            String numeroUN = String.valueOf(u) + String.valueOf(nConstant);
            String sha1password = DigestUtils.sha256Hex(numeroUN);
            return sha1password;

        }
        
        public String hashMenosUN(int u) {
            String numeroUN = String.valueOf(u) + String.valueOf(nConstant);
            String sha1password = DigestUtils.sha256Hex(numeroUN);
            return sha1password;

        }

    }
}
