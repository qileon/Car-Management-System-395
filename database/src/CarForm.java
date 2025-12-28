import javax.swing.*; //gui components
import javax.swing.table.DefaultTableModel; // data structure and updates for the jtable
import java.awt.*; // layouts, colors, fonts, and window events
import java.sql.*; // jdbc

// main window of the application
public class CarForm extends JFrame {

    private JTextField txtBrand;
    private JTextField txtModel;
    private JTextField txtYear;
    private JTextField txtPrice;

    private JTable table;
    private DefaultTableModel tableModel;

    // constructor
    public CarForm() {

        setTitle("car management system");
        setSize(750, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // creates the application panel with 5 rows and 2 columns
        JPanel panel = new JPanel(new GridLayout(5, 2));

        // creates labels and input fields for each text
        panel.add(new JLabel("brand"));
        txtBrand = new JTextField();
        panel.add(txtBrand);

        panel.add(new JLabel("model"));
        txtModel = new JTextField();
        panel.add(txtModel);

        panel.add(new JLabel("year"));
        txtYear = new JTextField();
        panel.add(txtYear);

        panel.add(new JLabel("price"));
        txtPrice = new JTextField();
        panel.add(txtPrice);

        // creates "add car" and delete selected car" buttons
        JButton btnAdd = new JButton("add car");
        JButton btnDelete = new JButton("delete selected car");

        panel.add(btnAdd);
        panel.add(btnDelete);

        // creates the area needed to display the car data from database
        add(panel, BorderLayout.NORTH)

        // creates column positions to organize data from database
        tableModel = new DefaultTableModel(
                new String[]{"id", "brand", "model", "year", "price"}, 0
        );

        // creates the table so data can be placed
        table = new JTable(tableModel);

        // disables editing for all table information cells, makes the table read-only
        table.setDefaultEditor(Object.class, null);

        // adds scrolling area when database data exceeds screen size
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadCars();

        btnAdd.addActionListener(e -> addCar());
        btnDelete.addActionListener(e -> deleteCar());
    }

    // load all cars
    private void loadCars() {

        // clears existing rows to prevent duplicates during reload
        tableModel.setRowCount(0);

        // after deletion, brings data back to screen
        String sql = "select * from cars";

        // connects to database using try catch and creates objects to run query
        try (Connection con = Database.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            // reads all rows inside the result set
            while (rs.next()) {

                //seperates data by data types and adds a new row
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getDouble("price")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // add new car
    private void addCar() {

        // gets car data from the user without empty spaces
        String brand = txtBrand.getText().trim();
        String model = txtModel.getText().trim();
        String yearText = txtYear.getText().trim();
        String priceText = txtPrice.getText().trim();

        // checks if there is any empty field
        if (brand.isEmpty() || model.isEmpty()
                || yearText.isEmpty() || priceText.isEmpty()) {

            // if there is an empty field, warns user and stops method here
            JOptionPane.showMessageDialog(this,
                    "all fields are required");
            return;
        }
        // checks the brand field contains only letters
        for (int i = 0; i < brand.length(); i++) {
            char c = brand.charAt(i);
            if (!Character.isLetter(c) && c != ' ') {
                // stops user from continuing
                JOptionPane.showMessageDialog(this,
                        "brand can only contain letters");
                return;
            }
        }
        // defines data types for year and price
        int year;
        double price;

        // checks if year and price arre entered correctly
        try {
            year = Integer.parseInt(yearText);
            price = Double.parseDouble(priceText);

        } catch (NumberFormatException e) {

            // shows error if there is number format exception
            JOptionPane.showMessageDialog(this,
                    "year and price must be numbers");
            return;
        }
        // checks the year range, if its not between 1900 and 2026
        if (year < 1900 || year > 2026) {
            // stops user again
            JOptionPane.showMessageDialog(this,
                    "year must be between 1900 and 2026");
            return;
        }
        // prepares sql code to check existing records
        String checkSql =
                "select count(*) from cars where brand=? and model=? and year=?";
        // checks brand, model and year values in database
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(checkSql)) {

            ps.setString(1, brand);
            ps.setString(2, model);
            ps.setInt(3, year);
            // query runs and moves to the next row
            ResultSet rs = ps.executeQuery();
            rs.next();
            // stops user again if same car exists
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this,
                        "this car already exists");
                return;
            }
            // shows error if database conncetion fails
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        // prepares sql insert query with placeholders for safety
        String insertSql =
                "insert into cars (brand, model, year, price) values (?, ?, ?, ?)";
        // establishes database connection using try with resources to ensure auto closing
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(insertSql)) {
            // assigns values to the '?' placeholders in the sql query
            ps.setString(1, brand);
            ps.setString(2, model);
            ps.setInt(3, year);
            ps.setDouble(4, price);

            // executes update operations
            ps.executeUpdate();
            loadCars();
            // clears input fields for new entries
            txtBrand.setText("");
            txtModel.setText("");
            txtYear.setText("");
            txtPrice.setText("");
            // prints sql error details
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteCar() {
        // gets the index of selected row in JTable
        int selectedRow = table.getSelectedRow();
        // warns user if no row is selected to prevent crash
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "please select a car first");
            return;
        }
        // gets ID value of selected car from table
        int id = (int) tableModel.getValueAt(selectedRow, 0);

        // creates a sql code to delete only selected record
        String sql = "delete from cars where id=?";

        // establishes database connection using try with resources to ensure auto closing
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // places selected car id into delete query
            ps.setInt(1, id);

            ps.executeUpdate();

            loadCars();

            //warns if database connection error occurs
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // main method runs the whole program, public allows access from other classes, static belongs to this class
    // void means it doesn't return any value, main is the method name, String[] args holds parameters given when the program runs
    public static void main(String[] args) {

        // used for thread management, waits until swing is ready
        SwingUtilities.invokeLater(() ->

                // shows the window created in CarForm()
                new CarForm().setVisible(true)
        );
    }

}
