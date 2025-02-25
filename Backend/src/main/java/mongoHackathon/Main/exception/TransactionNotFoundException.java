package mongoHackathon.Main.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String id) {
        super("Transaction not found with id: " + id);
    }
}
