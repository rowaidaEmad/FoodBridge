package foodbridge.models.donations;

import java.time.LocalDate;
import foodbridge.models.users.Donor;

public class Receipt {
    private int id;
    private Donation donation;
    private Donor donor;
    private LocalDate issueDate;
    private String description;

    public void generate() {
        // TODO
    }

    public void download() {
        // TODO
    }
}
