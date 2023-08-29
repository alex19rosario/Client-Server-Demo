package org.example;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Consumer<Integer> sendPerson = x -> {
            Runnable runnable = () -> JsonHandler.handleJson(new Person(x, "Carlos"));
            Thread thread = new Thread(runnable);
            thread.start();
        };

        for(int i = 0; i < 2000; i++){
            sendPerson.accept(i);
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

class JsonHandler{
    public static void handleJson(Person person){
        try{
            JsonObject obj = Json.createObjectBuilder()
                    .add("id", person.getId())
                    .add("name", person.getName())
                    .build();
            FileHandler.processBytes(obj);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void displayPerson(JsonObject obj){
        int id = obj.getInt("id");
        String name = obj.getString("name");
        Person person = new Person(id, name);
        System.out.println(person);
    }
}

class FileHandler{
    private static final String hostName = "10.0.0.14";
    private static final int portNumber = 9637;

    public static void processBytes(JsonObject obj){
        try (Socket socket = new Socket(hostName, portNumber);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream())){
            // Convert the JsonObject to a JSON string
            String jsonString = obj.toString();
            // Convert the JSON string to a byte array
            byte[] byteArray = jsonString.getBytes(StandardCharsets.UTF_8);
            // Write the length of the byte array first
            dos.writeInt(byteArray.length);
            // Write the byte array
            dos.write(byteArray);
            dos.flush();

            // Read the byte array from the input stream
            int length = dis.readInt();
            byte[] newByteArray = new byte[length];
            dis.readFully(newByteArray);
            // Convert the byte array to a String
            String newObjString = new String(newByteArray, StandardCharsets.UTF_8);
            // Parse the JSON string into a JsonObject
            JsonObject newObj = Json.createReader(new ByteArrayInputStream(newObjString.getBytes()))
                    .readObject();
            JsonHandler.displayPerson(newObj);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


