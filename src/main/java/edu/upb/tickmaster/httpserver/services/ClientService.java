package edu.upb.tickmaster.httpserver.services;

import edu.upb.tickmaster.httpserver.repositories.ClientRepository;
import org.mindrot.jbcrypt.BCrypt;

public class ClientService {
    private final ClientRepository repository = new ClientRepository();

    public int[] createClient(String user, String nombre, String pass, int rol) {
        String hashedPass = BCrypt.hashpw(pass, BCrypt.gensalt());
        return repository.insertClient(user, nombre, hashedPass, rol);
    }
}
