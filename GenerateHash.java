import at.favre.lib.crypto.bcrypt.BCrypt;

public class GenerateHash {
    public static void main(String[] args) {
        String password = "password123";
        
        // Test with cost factor 10 (like in seed data)
        String hash10 = BCrypt.withDefaults().hashToString(10, password.toCharArray());
        System.out.println("Cost 10: " + hash10);
        
        // Test with cost factor 12 (our current setting)
        String hash12 = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        System.out.println("Cost 12: " + hash12);
        
        // Test verification against seed hash
        String seedHash = "$2a$10$N9qo8uLOickgx2ZMRZoMye/SJ4y9hN7cU3VpQqPdIjVwLpEaJ1V5G";
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), seedHash);
        System.out.println("Seed hash verification: " + result.verified);
        System.out.println("Seed hash details: " + result.details);
    }
}