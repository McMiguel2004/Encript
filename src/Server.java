import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;

public class Server {
    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048); // Tamaño de la clave RSA
        return keyGen.generateKeyPair();
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Servidor iniciado. Esperando cliente...");

            // Generar par de claves para el servidor
            KeyPair serverKeyPair = generateKeyPair();

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado.");

                // Crear streams de entrada y salida
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                // Enviar clave pública del servidor al cliente
                ObjectOutputStream publicKeyOut = new ObjectOutputStream(out);
                publicKeyOut.writeObject(serverKeyPair.getPublic());
                publicKeyOut.flush();

                // Recibir clave pública del cliente
                ObjectInputStream publicKeyIn = new ObjectInputStream(in);
                PublicKey clientPublicKey = (PublicKey) publicKeyIn.readObject();

                // Crear cifrador con la clave pública del cliente
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, clientPublicKey);

                while (true) {
                    // Leer mensaje del cliente
                    StringBuilder messageBuilder = new StringBuilder();
                    int character;
                    while ((character = in.read()) != -1) {
                        if (character == '\n') {
                            break; // Fin del mensaje
                        }
                        messageBuilder.append((char) character);
                    }
                    String message = messageBuilder.toString().trim();
                    System.out.println("Cliente: " + message);

                    // Encriptar mensaje con clave pública del cliente
                    byte[] encryptedMessage = cipher.doFinal(message.getBytes());

                    // Enviar mensaje encriptado al cliente
                    out.writeInt(encryptedMessage.length);
                    out.write(encryptedMessage);
                    out.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
