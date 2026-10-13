
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;

// Main application and Swing GUI
public class Lab07A_Invoice extends JFrame {
    private final JTextField customerName = new JTextField(15);
    private final JTextField street = new JTextField(15);
    private final JTextField city = new JTextField(10);
    private final JTextField state = new JTextField(3);
    private final JTextField zip = new JTextField(6);

    private final JTextField productName = new JTextField(12);
    private final JTextField unitPrice = new JTextField(7);
    private final JTextField quantity = new JTextField(5);

    private final JTextArea invoiceOutput = new JTextArea(15, 45);

    private final Invoice invoice = new Invoice("INVOICE");

    public Lab07A_Invoice() {
        setTitle("Lab07A_Invoice");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder(
                "Customer Information"));

        form.add(new JLabel("Customer Name:"));
        form.add(customerName);
        form.add(new JLabel("Street Address:"));
        form.add(street);
        form.add(new JLabel("City:"));
        form.add(city);
        form.add(new JLabel("State:"));
        form.add(state);
        form.add(new JLabel("ZIP Code:"));
        form.add(zip);

        JPanel itemForm = new JPanel(new GridLayout(0, 2, 5, 5));
        itemForm.setBorder(BorderFactory.createTitledBorder(
                "Invoice Line Item"));

        itemForm.add(new JLabel("Product Name:"));
        itemForm.add(productName);
        itemForm.add(new JLabel("Unit Price ($):"));
        itemForm.add(unitPrice);
        itemForm.add(new JLabel("Quantity:"));
        itemForm.add(quantity);

        JButton addButton = new JButton("Add Line Item");
        JButton displayButton = new JButton("Display Invoice");

        addButton.addActionListener(e -> addLineItem());
        displayButton.addActionListener(e -> displayInvoice());

        JPanel buttons = new JPanel();
        buttons.add(addButton);
        buttons.add(displayButton);

        invoiceOutput.setEditable(false);
        invoiceOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(form, BorderLayout.NORTH);
        top.add(itemForm, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        setLayout(new BorderLayout(10, 10));
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(invoiceOutput), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addLineItem() {
        try {
            String name = productName.getText().trim();
            double price = Double.parseDouble(
                    unitPrice.getText().trim());
            int qty = Integer.parseInt(quantity.getText().trim());

            if (name.isEmpty() || price < 0 || qty <= 0) {
                throw new IllegalArgumentException(
                        "Enter a product name, a nonnegative price, "
                                + "and a quantity greater than zero.");
            }

            Product product = new Product(name, price);
            LineItem item = new LineItem(product, qty);
            invoice.addLineItem(item);

            JOptionPane.showMessageDialog(this,
                    "Line item added successfully.");

            productName.setText("");
            unitPrice.setText("");
            quantity.setText("");
            productName.requestFocus();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid price and whole-number quantity.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayInvoice() {
        if (invoice.getLineItems().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please add at least one line item.");
            return;
        }

        CustomerAddress address = new CustomerAddress(
                customerName.getText().trim(),
                street.getText().trim(),
                city.getText().trim(),
                state.getText().trim(),
                zip.getText().trim());

        invoice.setCustomerAddress(address);
        invoiceOutput.setText(invoice.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Lab07A_Invoice::new);
    }
}

// Invoice domain class
class Invoice {
    private String title;
    private CustomerAddress customerAddress;
    private final List<LineItem> lineItems = new ArrayList<>();

    public Invoice(String title) {
        this.title = title;
    }

    public void setCustomerAddress(CustomerAddress address) {
        this.customerAddress = address;
    }

    public void addLineItem(LineItem item) {
        lineItems.add(item);
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public double getTotalAmountDue() {
        double total = 0;
        for (LineItem item : lineItems) {
            total += item.getLineTotal();
        }
        return total;
    }

    @Override
    public String toString() {
        NumberFormat money = NumberFormat.getCurrencyInstance();
        StringBuilder result = new StringBuilder();

        result.append(title).append("\n");
        result.append("========================================\n");

        if (customerAddress != null) {
            result.append(customerAddress).append("\n");
        }

        result.append("----------------------------------------\n");
        result.append(String.format("%-18s %5s %10s %10s%n",
                "Product", "Qty", "Unit Price", "Total"));
        result.append("----------------------------------------\n");

        for (LineItem item : lineItems) {
            result.append(String.format("%-18s %5d %10s %10s%n",
                    item.getProduct().getName(),
                    item.getQuantity(),
                    money.format(item.getProduct().getUnitPrice()),
                    money.format(item.getLineTotal())));
        }

        result.append("----------------------------------------\n");
        result.append("TOTAL AMOUNT DUE: ")
                .append(money.format(getTotalAmountDue()))
                .append("\n");

        return result.toString();
    }
}

// LineItem domain class
class LineItem {
    private final Product product;
    private final int quantity;

    public LineItem(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "A product and positive quantity are required.");
        }
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getLineTotal() {
        return product.getUnitPrice() * quantity;
    }
}

// Product domain class
class Product {
    private final String name;
    private final double unitPrice;

    public Product(String name, double unitPrice) {
        if (name == null || name.trim().isEmpty()
                || !Double.isFinite(unitPrice) || unitPrice < 0) {
            throw new IllegalArgumentException(
                    "Invalid product name or unit price.");
        }
        this.name = name;
        this.unitPrice = unitPrice;
    }

    public String getName() {
        return name;
    }

    public double getUnitPrice() {
        return unitPrice;
    }
}

// CustomerAddress domain class
class CustomerAddress {
    private final String customerName;
    private final String street;
    private final String city;
    private final String state;
    private final String zipCode;

    public CustomerAddress(String customerName, String street,
                           String city, String state, String zipCode) {
        this.customerName = customerName;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }

    @Override
    public String toString() {
        return customerName + "\n"
                + street + "\n"
                + city + ", " + state + " " + zipCode;
    }
}
