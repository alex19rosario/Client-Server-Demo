package org.example;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        int portNumber = 9637;
        boolean keepRunning = true;
        try(ServerSocket ss = new ServerSocket(portNumber)) {

            ExecutorService executor = Executors.newFixedThreadPool(50);

            while(keepRunning){
                try{
                    Socket cs = ss.accept();
                    Runnable clientHandler = new ClientHandler(cs);
                    executor.execute(clientHandler);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class Person{
    private final int id;
    private final String name;

    public Person(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s,%s",id, name);
    }
}

class ClientHandler implements Runnable{

    private Socket socket;

    public ClientHandler(Socket socket){
        this.socket = socket;
    }

    public void run() {
        try (Socket cs = this.socket){
             JsonHandler.handleJson(cs);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}

class JsonHandler {

    public static void handleJson(Socket socket){
        try {
            JsonObject obj = FileHandler.processBytes(socket);
            int id = obj.getInt("id");
            String name = obj.getString("name");
            Person person = new Person(id, name);
            System.out.println(person);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static JsonObject processRequest(JsonObject obj){
        JsonObject newObj;
        try{
            newObj = Json.createObjectBuilder()
                    .add("id", obj.getInt("id") + 100)
                    .add("name", obj.getString("name"))
                    .build();
            return newObj;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

class FileHandler{

    public static JsonObject processBytes(Socket socket) {
        try(DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            // Read the byte array from the input stream
            int length = dis.readInt();
            byte[] byteArray = new byte[length];
            dis.readFully(byteArray);

            // Convert the byte array to a String
            String jsonString = new String(byteArray, StandardCharsets.UTF_8);
            // Parse the JSON string into a JsonObject
            JsonObject jsonObject = Json.createReader(new ByteArrayInputStream(jsonString.getBytes()))
                    .readObject();
            //Thread.sleep(5000);
            //Execute the query with the Json information and generate another Json to send to the client
            JsonObject newObj = JsonHandler.processRequest(jsonObject);
            // Convert the JsonObject to a JSON string
            String newObjString = newObj.toString();
            // Convert the JSON string to a byte array
            byte[] newByteArray = newObjString.getBytes(StandardCharsets.UTF_8);
            // Write the length of the new byte array first
            dos.writeInt(newByteArray.length);
            // Write the new byte array
            dos.write(newByteArray);
            dos.flush();

            return jsonObject;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
