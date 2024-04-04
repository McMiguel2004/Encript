import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 1234);
            System.out.println("Conectado al servidor.");

            // Crear streams de entrada y salida
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Recibir clave pública del servidor
            ObjectInputStream publicKeyIn = new ObjectInputStream(in);
            PublicKey serverPublicKey = (PublicKey) publicKeyIn.readObject();

            // Generar par de claves para el cliente
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048); // Tamaño de la clave RSA
            KeyPair clientKeyPair = keyGen.generateKeyPair();

            // Enviar clave pública del cliente al servidor
            ObjectOutputStream publicKeyOut = new ObjectOutputStream(out);
            publicKeyOut.writeObject(clientKeyPair.getPublic());
            publicKeyOut.flush();

            // Crear cifrador con la clave pública del servidor
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, clientKeyPair.getPrivate());

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                // Leer mensaje desde el teclado
                String message = reader.readLine();

                // Enviar mensaje al servidor con un salto de línea al final
                out.writeUTF(message + "\n");
                out.flush();

                // Leer respuesta del servidor
                int length = in.readInt();
                byte[] encryptedMessage = new byte[length];
                in.readFully(encryptedMessage, 0, length);

                // Desencriptar mensaje con clave privada del cliente
                byte[] decryptedMessage = cipher.doFinal(encryptedMessage);
                String decryptedString = new String(decryptedMessage);

                System.out.println("Servidor: " + decryptedString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
