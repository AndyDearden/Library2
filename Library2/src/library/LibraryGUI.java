package library;

import java.awt.Color;
import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import static java.lang.Integer.parseInt;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JButton;

public class LibraryGUI extends javax.swing.JFrame {

    private Book selectedBook = null;
    private Member selectedMember = null;
    private SetOfMembers theMembers = new SetOfMembers();
    private SetOfBooks theBooks = new SetOfBooks();
    private boolean changesSaved; // set when changes have been applied/changes saved
    private String queryInput = null; // user inputted value when searching

    /** Creates new form LibraryGUI */
    public LibraryGUI() 
    {
        initComponents();
        setGUIModalities(); // used to set all jDialogs to application modal

        hideFilterBtn(btnResetMembsFilter);
        hideFilterBtn(btnResetBooksFilter);
        
        // set to true for data-refresh
        File members = new File("Files/members.ser");
        File books = new File("Files/books.ser");
        
        if (!members.exists() || !books.exists()){
            File f = new File("Files/");
            if(!f.exists())
                f.mkdir();
            
            resetData();
        }else{
            deserializeMembersSet("Files/members.ser");
            deserializeBooksSet("Files/books.ser");
        }
        
        setGUITxt();
        refreshBooksJList(null);
        refreshMembersJList(null);
        
        // initially set to true until everything is loaded in
        changesSaved = true;
    }
    
    private void resetData(){
        Member member1 = new Member(0,"Glenn Rhee", null);
        Member member2 = new Member(1,"Daryl Dixon", null);
        Member member3 = new Member(2,"Rick Grimes", null);
        Member member4 = new Member(3,"Maggie Greene", null);
        Member member5 = new Member(4,"Hershel Greene", null);
        Member member6 = new Member(5,"The Governor", null);
        Member member7 = new Member(6,"Michonne", null);
        Member member8 = new Member(7,"Carol Peletier", null);

        theMembers.addMember(member1);
        theMembers.addMember(member2);
        theMembers.addMember(member3);
        theMembers.addMember(member4);
        theMembers.addMember(member5);
        theMembers.addMember(member6);
        theMembers.addMember(member7);
        theMembers.addMember(member8);
 
        Book book1 = new Book(0,"Game of Thrones", "George R. R. Martin", 1000000000 , null);
        Book book2 = new Book(1,"Harry Potter", "J.K. Rowling", 1111111111, null);
        Book book3 = new Book(2,"Great Expectations", "Charles Dickens", 222222222, null);
        Book book4 = new Book(3,"James and the Giant Peach", "Roald Dahl", 444444444, null);
        Book book5 = new Book(4,"Twilight", "Stephenie Meyer", 555555555, null);
        Book book6 = new Book(5,"The Da Vinci Code", "Dan Brown", 666666666, null);
        Book book7 = new Book(6,"To Kill a Mockingbird", "Harper Lee", 777777777, null);
        Book book8 = new Book(7,"The Lord of the Rings", "J. R. R. Tolkien", 888888888, null);

        theBooks.addBook(book1);
        theBooks.addBook(book2);
        theBooks.addBook(book3);
        theBooks.addBook(book4);
        theBooks.addBook(book5);
        theBooks.addBook(book6);
        theBooks.addBook(book7);
        theBooks.addBook(book8);
    }
    
    private void refreshLBooksLoanedJList(Member aMember){
        SetOfBooks loanedBooks = new SetOfBooks();
        Integer lBookCount;
        /* 
        Members currentLoans is currently set to wipe on serialization
        This forloop takes care of re-establishing the loans upon loading
        by looking at the Book class, and seeing who is the borrower. 
        */

        jListMembersLoanedBooks.setEnabled(true);
        if (aMember != null){
            for(Book book : theBooks) {
                if (book.getBorrower() != null){
                    if(aMember.getMemberNumber() == book.getBorrower().getMemberNumber()){
                        loanedBooks.addBook(book);
                    }
                }        
            }
            lBookCount = loanedBooks.size();
            jListMembersLoanedBooks.setListData(loanedBooks);
            
            if (lBookCount>2){
                lblLoanedBookCount.setText("("+String.valueOf(lBookCount)+" - Limit Reached!)");
                lblLoanedBookCount.setForeground(Color.decode("#FF0000"));
                jListMembersAvailBooks.setEnabled(false);
            }else{
                lblLoanedBookCount.setText("("+String.valueOf(lBookCount)+")");
                lblLoanedBookCount.setForeground(Color.decode("#00CC00"));
            }
            btnReturnBook.setEnabled(false);
        }else{
            // refresh list data to nothing
            jListMembersLoanedBooks.setListData(new Object[0]);
            jListMembersLoanedBooks.setEnabled(false);
        }
        btnReturnBook.setEnabled(false);
    }
        
    private void refreshABooksLoanedJList(Member aMember){
        if (aMember != null){
            jListMembersAvailBooks.setEnabled(true);
            jListMembersAvailBooks.setListData(theBooks.getAvailableBooks(theBooks)); 
            lblAvailBookCount.setText("("+String.valueOf(theBooks.getAvailableBooks(theBooks).size())+")");
        }else{
            jListMembersAvailBooks.setListData(new Object[0]);
            lblAvailBookCount.setText("(-)");
        }
        btnIssueLoan.setEnabled(false);
    }
    
    private void initiateBookTabLoan(){
        jDialogBookTabLoan.pack();
        jTxtBookTabLoanBookInput.setText(selectedBook.getTitle());
        jComboBookTabLoanMem.removeAllItems(); // reset combobox back to start
        
        for(Member mem: theMembers){
            //if(mem.getBooksOnLoan().size() < 3){
                jComboBookTabLoanMem.addItem(mem);
            //}
        }
        jDialogBookTabLoan.setVisible(true); 
    }
    
    private void initiateBookTabReturn(){
        jDialogBookTabReturn.pack();
        jTxtBookTabReturnMemInput.setText(selectedBook.getBorrower().getName());
        jTxtBookTabReturnBookInput.setText(selectedBook.getTitle());
        jDialogBookTabReturn.setVisible(true);
    }
    
    private void filterMembers(){
        SetOfMembers filtMems = new SetOfMembers();
        //clear selectedMember before begining
        clearSelectedMember();
        clearSelectedBook();
        if(jComboMemFilter.getSelectedIndex() == 0){
            // if combo box is 0, member is searching by ID
            try{
                filtMems = theMembers.getMembersFromSearch(parseInt(jTxtMemSearchInput.getText()));
            }catch(Exception e){
                // do something here
            }
        }
        else 
        {
            // if combo box is 1, member is searching by Name
            try{
                filtMems = theMembers.getMembersFromSearch(jTxtMemSearchInput.getText());
            }catch(Exception e){
                // do something here
            }
        }
        if (!filtMems.isEmpty()){
            refreshMembersJList(filtMems);
            refreshMemBookJLists();
            
            btnResetMembsFilter.setVisible(true);
            setGUITxt();
        }else{
            //error handler for when no results are returned...
            lblNoMemQResultsTxt.setVisible(true);
        }
        jDialogMemSearch.setVisible(false);
    }
    
    private void filterBooks(){
        SetOfBooks filtBooks = new SetOfBooks();
        //clear selectedBook before begining
        clearSelectedBook(); //need implementing
        if(jComboBookFilter.getSelectedIndex() == 0){
            // if combo box is 0, member is searching by book ID
            try{
                filtBooks = theBooks.findBookFromAccNumber(parseInt(jTxtBookSearchInput.getText()));
            }catch(Exception e){
                // do something here
            }
        }
        else if(jComboBookFilter.getSelectedIndex() == 1){
            // if combo box is 1, member is searching by book Title
            try{
                filtBooks = theBooks.findBookFromTitle(jTxtBookSearchInput.getText());
            }catch(Exception e){
                // do something here
            }
        }else if(jComboBookFilter.getSelectedIndex() == 2){
            // if combo box is 2, member is searching by book author
            try{
                filtBooks = theBooks.findBookByAuthor(jTxtBookSearchInput.getText());
            }catch(Exception e){
                // do something here
            }
        }else{
            // if combo box is anything else (only option is 3), member is searching by isbn
            try{
                filtBooks = theBooks.findBookFromISBN(parseInt(jTxtBookSearchInput.getText()));
            }catch(Exception e){
                // do something here
            }
        }
        if (!filtBooks.isEmpty()){
            refreshBooksJList(filtBooks);            
            btnResetBooksFilter.setVisible(true);
        }else{
            //error handler for when no results are returned...
            lblBookQResultsTxt.setVisible(true);
        }
        jDialogBookSearch.setVisible(false);
        
    }
    
    private void setGUIModalities(){
        jDialogBookSearch.setModalityType(APPLICATION_MODAL);
        jDialogQuitConf.setModalityType(APPLICATION_MODAL); 
        jDialogDelConf.setModalityType(APPLICATION_MODAL); 
        jDialogCRUDMemForm.setModalityType(APPLICATION_MODAL); 
        jDialogCRUDBookForm.setModalityType(APPLICATION_MODAL); 
        jDialogMemSearch.setModalityType(APPLICATION_MODAL);
        jDialogBookTabLoan.setModalityType(APPLICATION_MODAL);
        jDialogBookTabReturn.setModalityType(APPLICATION_MODAL);
    }
    
    private void hideFilterBtn(JButton filterBtn){
        filterBtn.setVisible(false);
        setGUITxt();
    }
    
    private void showChangesSaved(){
        if (changesSaved==false){
            lblChangesSaved.setVisible(true);
            changesSaved = true;
        }
    }
    
    private void setGUITxt(){
        lblNoMemQResultsTxt.setVisible(false);
        lblBookQResultsTxt.setVisible(false);
        lblChangesSaved.setVisible(false);
        changesSaved = false;
        // sets member/book count on main GUI
        lblMainMemberCnt.setText(String.valueOf(theMembers.size()));
        lblMainBookCnt.setText(String.valueOf(theBooks.size()));
    }
    
    private void saveAll(){
        clearSelectedMember();
        clearSelectedBook();
        serializeMembersSet("Files/members.ser");
        serializeBooksSet("Files/books.ser");
        showChangesSaved();
    }
    
    private void showMemSearchGUI(){        
        jDialogMemSearch.pack();
        jComboMemFilter.setSelectedIndex(0); // reset combo back to start
        jTxtMemSearchInput.setText("");
        jDialogMemSearch.setVisible(true);
    }
    
    private void showBookSearchGUI(){        
        jDialogBookSearch.pack();
        jComboBookFilter.setSelectedIndex(0); // reset combo back to start
        jTxtBookSearchInput.setText("");
        jDialogBookSearch.setVisible(true);
    }
    
    private void showQuitConfGUI(){
        if (changesSaved==false){
            jDialogQuitConf.pack(); 
            jDialogQuitConf.setVisible(true);
        }
        else{
            System.exit(0);
        }
    }
    
    private void showMemCRUDGUI(String reason) {
        jTxtMemNameInput.setEnabled(true);
        jTxtMemNameInput.setText("");
        jDialogCRUDMemForm.pack(); 
        
        if (reason == "new"){
            jTxtMemIDInput.setText(Integer.toString(
                    theMembers.lastElement().getMemberNumber()+1));
            btnMemFormApply.setText("Add");
        }else{
            jTxtMemIDInput.setText(String.valueOf(
                selectedMember.getMemberNumber()));
            jTxtMemNameInput.setText(selectedMember.getName());
            
            if(reason == "edit"){
                btnMemFormApply.setText("Save");
            }else{
                btnMemFormApply.setText("Delete");
                jTxtMemNameInput.setEnabled(false);
            }
        }
        jDialogCRUDMemForm.setVisible(true);
    }
    
    private void showBookCRUDGUI(String reason) {
        jTxtBookTitleInput.setEnabled(true);
        jTxtBookTitleInput.setText("");
        jTxtBookAuthorInput.setEnabled(true);
        jTxtBookAuthorInput.setText("");
        jTxtBookISBNInput.setEnabled(true);
        jTxtBookISBNInput.setText("");
        jDialogCRUDBookForm.pack();
        
        if (reason == "new"){
            jTxtBookAccNoInput.setText(Integer.toString(
                    theBooks.lastElement().getBookAccNo()+1));
            btnBookFormApply.setText("Add");
        }else{
            jTxtBookAccNoInput.setText(String.valueOf(
                selectedBook.getBookAccNo()));
            jTxtBookTitleInput.setText(selectedBook.getTitle());
            jTxtBookAuthorInput.setText(selectedBook.getAuthor());
            jTxtBookISBNInput.setText(String.valueOf(selectedBook.getISBNNumber()));
            if(reason == "edit"){
                btnBookFormApply.setText("Save");
            }else{
                btnBookFormApply.setText("Delete");
                jTxtBookTitleInput.setEnabled(false);
                jTxtBookAuthorInput.setEnabled(false);
                jTxtBookISBNInput.setEnabled(false);
            }
        }
        lblBookCountVal.setText(Integer.toString(theBooks.size()));
        jDialogCRUDBookForm.setVisible(true); 
    }
    
    public void loanBook(){
        
    }
    
    public void acceptReturn(){
        
    }
    
    public void showCurrentLoans(){
        
    }
    
    public void selectBook(){
        
    }
    
    public void selectMember(){
        
    }
    
    private void refreshMembersJList(SetOfMembers filtMembers){
        if (filtMembers == null){
            jListMembers.setListData(theMembers);
        }else{
            jListMembers.setListData(filtMembers);
        }
    }
    
    private void refreshBooksJList(SetOfBooks filtBooks){
        if (filtBooks == null){
            jListBooks.setListData(theBooks);
        }else{
            jListBooks.setListData(filtBooks);
        }
    }
    
    
    private void setSelectedMember(){
        // Get value from jlist (SetOfMembers) and cast to member
        selectedMember = (Member)jListMembers.getSelectedValue();
        btnEditMem.setEnabled(true);
        
        SetOfBooks loanedBooks = new SetOfBooks();
        for(Book book : theBooks) {
            if (book.getBorrower() != null){
                if(selectedMember.getMemberNumber() == book.getBorrower().getMemberNumber()){
                    loanedBooks.addBook(book);
                }
            }        
        }
        if (loanedBooks.isEmpty()){
            btnDelMem.setEnabled(true);
            btnDelMem.setToolTipText(null);
        }else{
            btnDelMem.setEnabled(false);
            btnDelMem.setToolTipText("Can't delete - Member has book/s on loan");
        } 
    }
    
    private void setSelectedBook(){
        // Get value from jlist (SetOfMembers) and cast to member
        selectedBook = (Book)jListBooks.getSelectedValue();
        btnEditBook.setEnabled(true);
        
        if (selectedBook.getBorrower() == null){
            btnReturnBookTab.setEnabled(false);
            btnIssueLoanBookTab.setEnabled(true);
            btnDelBook.setEnabled(true);
            btnDelBook.setToolTipText(null);
        }else{
            btnReturnBookTab.setEnabled(true);
            btnIssueLoanBookTab.setEnabled(false);
            btnDelBook.setEnabled(false);
            
            btnDelBook.setToolTipText("Can't delete - Member has book/s on loan");       
        }
    }
    
    private void clearSelectedMember(){
        jListMembers.clearSelection();
        lblNoMemQResultsTxt.setVisible(false);
        selectedMember = null;
        lblLoanedBookCount.setText("(-)");
        lblLoanedBookCount.setForeground(Color.decode("#000000"));
        lblAvailBookCount.setText("(-)");
        lblAvailBookCount.setForeground(Color.decode("#000000"));
        refreshMemBookJLists();
        refreshMembersJList(null);
        btnEditMem.setEnabled(false);
        btnDelMem.setEnabled(false);
    }
    private void clearSelectedBook(){
        jListBooks.clearSelection();
        lblBookQResultsTxt.setVisible(false);
        selectedBook = null;
        //refreshBooksJList(null);
        btnEditBook.setEnabled(false);
        btnDelBook.setEnabled(false);
        btnIssueLoanBookTab.setEnabled(false);
        btnReturnBookTab.setEnabled(false);
    }
    
    private void refreshMemBookJLists(){
        refreshABooksLoanedJList(selectedMember); 
        refreshLBooksLoanedJList(selectedMember);
        
    }
   
    private void serializeMembersSet(String fLoc){
        try
        {
            FileOutputStream fileOut = 
                    new FileOutputStream(fLoc);
            ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
            
            for(Member mem : theMembers) {
                /* Function setup for when data is loaded back in, so books  
                can be re-initialized without dups */
                mem.returnAllBOL();    // return all books on loan per member
            }
            objOut.writeObject(theMembers);
            
            objOut.close();
            fileOut.close();
            System.out.println("Serialized data is saved in " + fLoc);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        // completely refresh data, if its been saved without quitting
        deserializeMembersSet(fLoc);
    }

    private void serializeBooksSet(String fLoc){
        try
        {
            FileOutputStream fileOut = 
                    new FileOutputStream(fLoc);
            ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
            objOut.writeObject(theBooks);
            objOut.close();
            fileOut.close();
            System.out.println("Serialized data is saved in " + fLoc);
        }catch(IOException e)
        {
            e.printStackTrace();
        }
        // completely refresh data, if its been saved without quitting
        deserializeBooksSet(fLoc);
    }
    private void deserializeMembersSet(String fLoc)
    {
        //failsafe
        theMembers.removeAllElements();
        try
        {
            FileInputStream fileIn = new FileInputStream(fLoc);
            ObjectInputStream objIn = new ObjectInputStream (fileIn);

            Object obj = objIn.readObject();
            
            // check read object
            if (obj instanceof Vector)
            {
                SetOfMembers eMembers = (SetOfMembers) obj;
                for(int x = 0; x < eMembers.size(); x++){
                    Member eMem = new Member(eMembers.elementAt(x).getMemberNumber(),
                            eMembers.elementAt(x).getName(), 
                            eMembers.elementAt(x).getBooksOnLoan());
                    theMembers.addMember(eMem);
                }
            }
            fileIn.close();
            objIn.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return;
        }
        catch(ClassNotFoundException c)
        {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }
    }
    
    private void deserializeBooksSet(String fLoc)
    {
        //failsafe
        theBooks.removeAllElements();
        try
        {
            FileInputStream fileIn = new FileInputStream(fLoc);
            ObjectInputStream objIn = new ObjectInputStream (fileIn);

            Object obj = objIn.readObject();
            
            // check read object
            if (obj instanceof Vector)
            {
                SetOfBooks eBooks = (SetOfBooks) obj; // existing books
                for(int x = 0; x < eBooks.size(); x++){
                    Book eBook = new Book(eBooks.elementAt(x).getBookAccNo(),eBooks.elementAt(x).getTitle(),
                            eBooks.elementAt(x).getAuthor(),eBooks.elementAt(x).getISBNNumber(), 
                            eBooks.elementAt(x).getBorrower());
                    theBooks.addBook(eBook);
                }
            }
            fileIn.close();
            objIn.close();
            
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return;
        }
        catch(ClassNotFoundException c)
        {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialogBookSearch = new javax.swing.JDialog();
        jSeparatorBookSearchHeader = new javax.swing.JSeparator();
        lblBookSearchFilterTxt = new javax.swing.JLabel();
        lblBookSearchInputTxt = new javax.swing.JLabel();
        jComboBookFilter = new javax.swing.JComboBox();
        jTxtBookSearchInput = new javax.swing.JTextField();
        lblBookSearchTitle = new javax.swing.JLabel();
        btnBookSearch = new javax.swing.JButton();
        jDialogQuitConf = new javax.swing.JDialog();
        btnQuitConfJQ = new javax.swing.JButton();
        btnQuitConfSQ = new javax.swing.JButton();
        lblQuitConfTitle = new javax.swing.JLabel();
        jSeparatorQuitConfHeader = new javax.swing.JSeparator();
        jDialogDelConf = new javax.swing.JDialog();
        btnQuitConfYes = new javax.swing.JButton();
        lblDelConfTitle = new javax.swing.JLabel();
        jSeparatorDelConfHeader = new javax.swing.JSeparator();
        btnQuitConfNo = new javax.swing.JButton();
        jDialogCRUDMemForm = new javax.swing.JDialog();
        lblMemFormTitle = new javax.swing.JLabel();
        btnMemFormCancel = new javax.swing.JButton();
        jSeparatorMemFormHeader = new javax.swing.JSeparator();
        btnMemFormApply = new javax.swing.JButton();
        jTxtMemNameInput = new javax.swing.JTextPane();
        lblMemNameInputTxt = new javax.swing.JLabel();
        jTxtMemIDInput = new javax.swing.JTextPane();
        lblMemIDInputTxt = new javax.swing.JLabel();
        jDialogCRUDBookForm = new javax.swing.JDialog();
        lblBookFormHeader = new javax.swing.JLabel();
        btnBookFormCancel = new javax.swing.JButton();
        jSeparatorBookFormHeader = new javax.swing.JSeparator();
        jTxtBookTitleInput = new javax.swing.JTextPane();
        btnBookFormApply = new javax.swing.JButton();
        lblBookTitleTxt = new javax.swing.JLabel();
        jTxtBookAuthorInput = new javax.swing.JTextPane();
        lblBookAuthorTxt = new javax.swing.JLabel();
        lblBookISBNTxt = new javax.swing.JLabel();
        jTxtBookISBNInput = new javax.swing.JTextPane();
        lblBookAccNoTxt = new javax.swing.JLabel();
        jTxtBookAccNoInput = new javax.swing.JTextPane();
        lblBookCountVal = new javax.swing.JLabel();
        lblBookCountTxt = new javax.swing.JLabel();
        jDialogMemSearch = new javax.swing.JDialog();
        lblMemSearchTitle = new javax.swing.JLabel();
        btnMemSearch = new javax.swing.JButton();
        jSeparatorMemSearchHeader = new javax.swing.JSeparator();
        lblMemSearchFilterTxt = new javax.swing.JLabel();
        lblMemSearchInputTxt = new javax.swing.JLabel();
        jComboMemFilter = new javax.swing.JComboBox();
        jTxtMemSearchInput = new javax.swing.JTextField();
        jDialogBookTabLoan = new javax.swing.JDialog();
        lblBookTabLoanBookTxt = new javax.swing.JLabel();
        jSeparatorBookSearchHeader1 = new javax.swing.JSeparator();
        lblBookTabLoanTitle = new javax.swing.JLabel();
        btnBookTabLoan = new javax.swing.JButton();
        jTxtBookTabLoanBookInput = new javax.swing.JTextField();
        btnBookTabLoanCancel = new javax.swing.JButton();
        lblBookTabLoanMemTxt = new javax.swing.JLabel();
        jComboBookTabLoanMem = new javax.swing.JComboBox();
        jDialogBookTabReturn = new javax.swing.JDialog();
        lblBookTabReturnMemTxt = new javax.swing.JLabel();
        lblBookTabReturnBookTxt = new javax.swing.JLabel();
        jSeparatorBookSearchHeader2 = new javax.swing.JSeparator();
        lblBookTabReturnTitle = new javax.swing.JLabel();
        btnBookTabReturn = new javax.swing.JButton();
        btnBookTabReturnCancel = new javax.swing.JButton();
        jTxtBookTabReturnMemInput = new javax.swing.JTextField();
        jTxtBookTabReturnBookInput = new javax.swing.JTextField();
        jPanelContainer = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        jtabMainInterface = new javax.swing.JTabbedPane();
        jPanelMembers = new javax.swing.JPanel();
        btnQueryMembers = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListMembersLoanedBooks = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListMembers = new javax.swing.JList();
        lblBooksLoaned = new javax.swing.JLabel();
        lblMembersJlistHead = new javax.swing.JLabel();
        btnDelMem = new javax.swing.JButton();
        btnAddMem = new javax.swing.JButton();
        btnEditMem = new javax.swing.JButton();
        btnResetMembsFilter = new javax.swing.JButton();
        lblAvailableBooks = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jListMembersAvailBooks = new javax.swing.JList();
        btnIssueLoan = new javax.swing.JButton();
        btnReturnBook = new javax.swing.JButton();
        lblNoMemQResultsTxt = new javax.swing.JLabel();
        lblLoanedBookCount = new javax.swing.JLabel();
        lblAvailBookCount = new javax.swing.JLabel();
        jPanelBooks = new javax.swing.JPanel();
        btnQueryBooks = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListBooks = new javax.swing.JList();
        lblBooksJlistHead = new javax.swing.JLabel();
        btnAddBook = new javax.swing.JButton();
        btnDelBook = new javax.swing.JButton();
        btnEditBook = new javax.swing.JButton();
        btnResetBooksFilter = new javax.swing.JButton();
        btnQueryFilterAvail = new javax.swing.JButton();
        btnQueryFilterUnavail = new javax.swing.JButton();
        btnIssueLoanBookTab = new javax.swing.JButton();
        btnReturnBookTab = new javax.swing.JButton();
        lblBookQResultsTxt = new javax.swing.JLabel();
        btnSaveChanges = new javax.swing.JButton();
        btnQuit = new javax.swing.JButton();
        lblChangesSaved = new javax.swing.JLabel();
        lblMainMemberCntTxt = new javax.swing.JLabel();
        lblMainBookCntTxt = new javax.swing.JLabel();
        lblMainMemberCnt = new javax.swing.JLabel();
        lblMainBookCnt = new javax.swing.JLabel();

        jDialogBookSearch.setLocationByPlatform(true);
        jDialogBookSearch.setMinimumSize(new java.awt.Dimension(470, 179));
        jDialogBookSearch.setResizable(false);
        jDialogBookSearch.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                jDialogBookSearchWindowActivated(evt);
            }
        });

        lblBookSearchFilterTxt.setText("Search By");

        lblBookSearchInputTxt.setText("Search Value");

        jComboBookFilter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ID", "Title", "Author", "ISBN Number" }));

        jTxtBookSearchInput.setMinimumSize(new java.awt.Dimension(72, 26));
        jTxtBookSearchInput.setPreferredSize(new java.awt.Dimension(72, 26));

        lblBookSearchTitle.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblBookSearchTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBookSearchTitle.setText("Book Search");

        btnBookSearch.setText("Search");
        btnBookSearch.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnBookSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBookSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jDialogBookSearchLayout = new javax.swing.GroupLayout(jDialogBookSearch.getContentPane());
        jDialogBookSearch.getContentPane().setLayout(jDialogBookSearchLayout);
        jDialogBookSearchLayout.setHorizontalGroup(
            jDialogBookSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogBookSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogBookSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparatorBookSearchHeader)
                    .addComponent(lblBookSearchTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialogBookSearchLayout.createSequentialGroup()
                .addGap(0, 25, Short.MAX_VALUE)
                .addGroup(jDialogBookSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnBookSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTxtBookSearchInput, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jDialogBookSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jDialogBookSearchLayout.createSequentialGroup()
                            .addComponent(lblBookSearchFilterTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(44, 44, 44)
                            .addComponent(jComboBookFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(lblBookSearchInputTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(25, 25, 25))
        );
        jDialogBookSearchLayout.setVerticalGroup(
            jDialogBookSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogBookSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblBookSearchTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparatorBookSearchHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addGroup(jDialogBookSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBookSearchFilterTxt)
                    .addComponent(jComboBookFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jDialogBookSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBookSearchInputTxt)
                    .addComponent(jTxtBookSearchInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnBookSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        jDialogQuitConf.setLocationByPlatform(true);
        jDialogQuitConf.setMinimumSize(new java.awt.Dimension(503, 169));
        jDialogQuitConf.setResizable(false);

        btnQuitConfJQ.setText("Just Quit");
        btnQuitConfJQ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitConfJQActionPerformed(evt);
            }
        });

        btnQuitConfSQ.setText("Save & Quit");
        btnQuitConfSQ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitConfSQActionPerformed(evt);
            }
        });

        lblQuitConfTitle.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblQuitConfTitle.setText("Would you like to save before quitting?");

        javax.swing.GroupLayout jDialogQuitConfLayout = new javax.swing.GroupLayout(jDialogQuitConf.getContentPane());
        jDialogQuitConf.getContentPane().setLayout(jDialogQuitConfLayout);
        jDialogQuitConfLayout.setHorizontalGroup(
            jDialogQuitConfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogQuitConfLayout.createSequentialGroup()
                .addGroup(jDialogQuitConfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jDialogQuitConfLayout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(jDialogQuitConfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblQuitConfTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSeparatorQuitConfHeader)))
                    .addGroup(jDialogQuitConfLayout.createSequentialGroup()
                        .addGap(159, 159, 159)
                        .addComponent(btnQuitConfJQ)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuitConfSQ)))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jDialogQuitConfLayout.setVerticalGroup(
            jDialogQuitConfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogQuitConfLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(lblQuitConfTitle)
                .addGap(18, 18, 18)
                .addComponent(jSeparatorQuitConfHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jDialogQuitConfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnQuitConfJQ)
                    .addComponent(btnQuitConfSQ))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jDialogDelConf.setLocationByPlatform(true);
        jDialogDelConf.setMinimumSize(new java.awt.Dimension(453, 165));
        jDialogDelConf.setResizable(false);

        btnQuitConfYes.setText("Yes");
        btnQuitConfYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitConfYesActionPerformed(evt);
            }
        });

        lblDelConfTitle.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblDelConfTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDelConfTitle.setText("Are you sure you want to delete this?");

        btnQuitConfNo.setText("No");
        btnQuitConfNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitConfNoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jDialogDelConfLayout = new javax.swing.GroupLayout(jDialogDelConf.getContentPane());
        jDialogDelConf.getContentPane().setLayout(jDialogDelConfLayout);
        jDialogDelConfLayout.setHorizontalGroup(
            jDialogDelConfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogDelConfLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogDelConfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparatorDelConfHeader)
                    .addComponent(lblDelConfTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialogDelConfLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnQuitConfNo, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnQuitConfYes, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(141, 141, 141))
        );
        jDialogDelConfLayout.setVerticalGroup(
            jDialogDelConfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogDelConfLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(lblDelConfTitle)
                .addGap(18, 18, 18)
                .addComponent(jSeparatorDelConfHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jDialogDelConfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnQuitConfNo)
                    .addComponent(btnQuitConfYes))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        jDialogCRUDMemForm.setLocationByPlatform(true);
        jDialogCRUDMemForm.setMinimumSize(new java.awt.Dimension(460, 226));

        lblMemFormTitle.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblMemFormTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMemFormTitle.setText("Member Form");

        btnMemFormCancel.setText("Cancel");
        btnMemFormCancel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnMemFormCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMemFormCancelActionPerformed(evt);
            }
        });

        btnMemFormApply.setText("variable");
        btnMemFormApply.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnMemFormApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMemFormApplyActionPerformed(evt);
            }
        });

        jTxtMemNameInput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTxtMemNameInput.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N

        lblMemNameInputTxt.setText("Members Name");

        jTxtMemIDInput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTxtMemIDInput.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        jTxtMemIDInput.setText("number");
        jTxtMemIDInput.setToolTipText("");
        jTxtMemIDInput.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jTxtMemIDInput.setEnabled(false);
        jTxtMemIDInput.setFocusable(false);

        lblMemIDInputTxt.setText("Member ID");

        javax.swing.GroupLayout jDialogCRUDMemFormLayout = new javax.swing.GroupLayout(jDialogCRUDMemForm.getContentPane());
        jDialogCRUDMemForm.getContentPane().setLayout(jDialogCRUDMemFormLayout);
        jDialogCRUDMemFormLayout.setHorizontalGroup(
            jDialogCRUDMemFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogCRUDMemFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogCRUDMemFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparatorMemFormHeader)
                    .addComponent(lblMemFormTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialogCRUDMemFormLayout.createSequentialGroup()
                        .addGroup(jDialogCRUDMemFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jDialogCRUDMemFormLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnMemFormCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnMemFormApply, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jDialogCRUDMemFormLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(jDialogCRUDMemFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblMemIDInputTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblMemNameInputTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(jDialogCRUDMemFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTxtMemNameInput, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTxtMemIDInput, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(16, 16, 16)))
                .addContainerGap())
        );
        jDialogCRUDMemFormLayout.setVerticalGroup(
            jDialogCRUDMemFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogCRUDMemFormLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMemFormTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparatorMemFormHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jDialogCRUDMemFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTxtMemIDInput, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                    .addComponent(lblMemIDInputTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jDialogCRUDMemFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblMemNameInputTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTxtMemNameInput, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jDialogCRUDMemFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMemFormCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMemFormApply, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jDialogCRUDBookForm.setLocationByPlatform(true);
        jDialogCRUDBookForm.setMinimumSize(new java.awt.Dimension(529, 387));
        jDialogCRUDBookForm.setResizable(false);

        lblBookFormHeader.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblBookFormHeader.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBookFormHeader.setText("Book Form");

        btnBookFormCancel.setText("Cancel");
        btnBookFormCancel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnBookFormCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBookFormCancelActionPerformed(evt);
            }
        });

        jTxtBookTitleInput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTxtBookTitleInput.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N

        btnBookFormApply.setText("variable");
        btnBookFormApply.setToolTipText("");
        btnBookFormApply.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnBookFormApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBookFormApplyActionPerformed(evt);
            }
        });

        lblBookTitleTxt.setText("Title");

        jTxtBookAuthorInput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTxtBookAuthorInput.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N

        lblBookAuthorTxt.setText("Author");

        lblBookISBNTxt.setText("ISBN Number");

        jTxtBookISBNInput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTxtBookISBNInput.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N

        lblBookAccNoTxt.setText("Accession Number");
        lblBookAccNoTxt.setMaximumSize(new java.awt.Dimension(31, 20));
        lblBookAccNoTxt.setMinimumSize(new java.awt.Dimension(31, 20));
        lblBookAccNoTxt.setPreferredSize(new java.awt.Dimension(31, 20));

        jTxtBookAccNoInput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTxtBookAccNoInput.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        jTxtBookAccNoInput.setText("number");
        jTxtBookAccNoInput.setToolTipText("");
        jTxtBookAccNoInput.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jTxtBookAccNoInput.setEnabled(false);
        jTxtBookAccNoInput.setFocusable(false);

        lblBookCountVal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBookCountVal.setText("Number");

        lblBookCountTxt.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBookCountTxt.setText("Current Book Count:");

        javax.swing.GroupLayout jDialogCRUDBookFormLayout = new javax.swing.GroupLayout(jDialogCRUDBookForm.getContentPane());
        jDialogCRUDBookForm.getContentPane().setLayout(jDialogCRUDBookFormLayout);
        jDialogCRUDBookFormLayout.setHorizontalGroup(
            jDialogCRUDBookFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogCRUDBookFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogCRUDBookFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparatorBookFormHeader)
                    .addComponent(lblBookFormHeader, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jDialogCRUDBookFormLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jDialogCRUDBookFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jDialogCRUDBookFormLayout.createSequentialGroup()
                                .addGroup(jDialogCRUDBookFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblBookTitleTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblBookAuthorTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblBookISBNTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                                    .addComponent(lblBookAccNoTxt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(jDialogCRUDBookFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTxtBookISBNInput, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTxtBookAuthorInput, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTxtBookTitleInput, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTxtBookAccNoInput, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jDialogCRUDBookFormLayout.createSequentialGroup()
                                .addComponent(lblBookCountTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblBookCountVal)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnBookFormCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnBookFormApply, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(16, 16, 16)))
                .addContainerGap())
        );
        jDialogCRUDBookFormLayout.setVerticalGroup(
            jDialogCRUDBookFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogCRUDBookFormLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblBookFormHeader)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparatorBookFormHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addGroup(jDialogCRUDBookFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblBookAccNoTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTxtBookAccNoInput, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jDialogCRUDBookFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblBookTitleTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTxtBookTitleInput, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jDialogCRUDBookFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblBookAuthorTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTxtBookAuthorInput, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jDialogCRUDBookFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblBookISBNTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTxtBookISBNInput, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jDialogCRUDBookFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jDialogCRUDBookFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblBookCountTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblBookCountVal))
                    .addGroup(jDialogCRUDBookFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnBookFormCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnBookFormApply, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(17, 17, 17))
        );

        jDialogMemSearch.setLocationByPlatform(true);
        jDialogMemSearch.setMinimumSize(new java.awt.Dimension(470, 257));
        jDialogMemSearch.setResizable(false);
        jDialogMemSearch.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                jDialogMemSearchWindowActivated(evt);
            }
        });

        lblMemSearchTitle.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblMemSearchTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMemSearchTitle.setText("Member Search");

        btnMemSearch.setText("Search");
        btnMemSearch.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnMemSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMemSearchActionPerformed(evt);
            }
        });

        lblMemSearchFilterTxt.setText("Search By");

        lblMemSearchInputTxt.setText("Search Value");

        jComboMemFilter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ID", "Name" }));

        jTxtMemSearchInput.setMinimumSize(new java.awt.Dimension(72, 26));
        jTxtMemSearchInput.setName(""); // NOI18N
        jTxtMemSearchInput.setPreferredSize(new java.awt.Dimension(72, 26));

        javax.swing.GroupLayout jDialogMemSearchLayout = new javax.swing.GroupLayout(jDialogMemSearch.getContentPane());
        jDialogMemSearch.getContentPane().setLayout(jDialogMemSearchLayout);
        jDialogMemSearchLayout.setHorizontalGroup(
            jDialogMemSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogMemSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogMemSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparatorMemSearchHeader)
                    .addComponent(lblMemSearchTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialogMemSearchLayout.createSequentialGroup()
                .addGap(0, 25, Short.MAX_VALUE)
                .addGroup(jDialogMemSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnMemSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jDialogMemSearchLayout.createSequentialGroup()
                        .addGroup(jDialogMemSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblMemSearchFilterTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblMemSearchInputTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                        .addGap(44, 44, 44)
                        .addGroup(jDialogMemSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jComboMemFilter, 0, 236, Short.MAX_VALUE)
                            .addComponent(jTxtMemSearchInput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(25, 25, 25))
        );
        jDialogMemSearchLayout.setVerticalGroup(
            jDialogMemSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogMemSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMemSearchTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparatorMemSearchHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jDialogMemSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMemSearchFilterTxt)
                    .addComponent(jComboMemFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jDialogMemSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMemSearchInputTxt)
                    .addComponent(jTxtMemSearchInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addComponent(btnMemSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        jDialogBookTabLoan.setLocationByPlatform(true);
        jDialogBookTabLoan.setMinimumSize(new java.awt.Dimension(400, 220));
        jDialogBookTabLoan.setResizable(false);
        jDialogBookTabLoan.setSize(new java.awt.Dimension(400, 220));

        lblBookTabLoanBookTxt.setText("Book to loan:");

        lblBookTabLoanTitle.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblBookTabLoanTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBookTabLoanTitle.setText("Book-Tab Loan");

        btnBookTabLoan.setText("Loan");
        btnBookTabLoan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnBookTabLoan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBookTabLoanActionPerformed(evt);
            }
        });

        jTxtBookTabLoanBookInput.setEnabled(false);
        jTxtBookTabLoanBookInput.setMinimumSize(new java.awt.Dimension(72, 26));
        jTxtBookTabLoanBookInput.setPreferredSize(new java.awt.Dimension(72, 26));

        btnBookTabLoanCancel.setText("Cancel");
        btnBookTabLoanCancel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnBookTabLoanCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBookTabLoanCancelActionPerformed(evt);
            }
        });

        lblBookTabLoanMemTxt.setText("Loan book to:");

        jComboBookTabLoanMem.setToolTipText("");
        jComboBookTabLoanMem.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBookTabLoanMemItemStateChanged(evt);
            }
        });
        jComboBookTabLoanMem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBookTabLoanMemActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jDialogBookTabLoanLayout = new javax.swing.GroupLayout(jDialogBookTabLoan.getContentPane());
        jDialogBookTabLoan.getContentPane().setLayout(jDialogBookTabLoanLayout);
        jDialogBookTabLoanLayout.setHorizontalGroup(
            jDialogBookTabLoanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialogBookTabLoanLayout.createSequentialGroup()
                .addGap(0, 163, Short.MAX_VALUE)
                .addComponent(btnBookTabLoanCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnBookTabLoan, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
            .addGroup(jDialogBookTabLoanLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogBookTabLoanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jDialogBookTabLoanLayout.createSequentialGroup()
                        .addGroup(jDialogBookTabLoanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparatorBookSearchHeader1)
                            .addComponent(lblBookTabLoanTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(jDialogBookTabLoanLayout.createSequentialGroup()
                        .addComponent(lblBookTabLoanBookTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(250, 250, 250))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialogBookTabLoanLayout.createSequentialGroup()
                        .addComponent(lblBookTabLoanMemTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jDialogBookTabLoanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTxtBookTabLoanBookInput, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                            .addComponent(jComboBookTabLoanMem, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(17, 17, 17))))
        );
        jDialogBookTabLoanLayout.setVerticalGroup(
            jDialogBookTabLoanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogBookTabLoanLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblBookTabLoanTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparatorBookSearchHeader1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jDialogBookTabLoanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBookTabLoanBookTxt)
                    .addComponent(jTxtBookTabLoanBookInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jDialogBookTabLoanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBookTabLoanMem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBookTabLoanMemTxt))
                .addGap(26, 26, 26)
                .addGroup(jDialogBookTabLoanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBookTabLoan, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBookTabLoanCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(35, Short.MAX_VALUE))
        );

        jDialogBookTabReturn.setLocationByPlatform(true);
        jDialogBookTabReturn.setMinimumSize(new java.awt.Dimension(400, 220));
        jDialogBookTabReturn.setResizable(false);

        lblBookTabReturnMemTxt.setText("Current Borrower: ");

        lblBookTabReturnBookTxt.setText("Book to Return: ");

        lblBookTabReturnTitle.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblBookTabReturnTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBookTabReturnTitle.setText("Book-Tab Return");

        btnBookTabReturn.setText("Return");
        btnBookTabReturn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnBookTabReturn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBookTabReturnActionPerformed(evt);
            }
        });

        btnBookTabReturnCancel.setText("Cancel");
        btnBookTabReturnCancel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnBookTabReturnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBookTabReturnCancelActionPerformed(evt);
            }
        });

        jTxtBookTabReturnMemInput.setEnabled(false);
        jTxtBookTabReturnMemInput.setMinimumSize(new java.awt.Dimension(72, 26));
        jTxtBookTabReturnMemInput.setPreferredSize(new java.awt.Dimension(72, 26));

        jTxtBookTabReturnBookInput.setEnabled(false);
        jTxtBookTabReturnBookInput.setMinimumSize(new java.awt.Dimension(72, 26));
        jTxtBookTabReturnBookInput.setPreferredSize(new java.awt.Dimension(72, 26));

        javax.swing.GroupLayout jDialogBookTabReturnLayout = new javax.swing.GroupLayout(jDialogBookTabReturn.getContentPane());
        jDialogBookTabReturn.getContentPane().setLayout(jDialogBookTabReturnLayout);
        jDialogBookTabReturnLayout.setHorizontalGroup(
            jDialogBookTabReturnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialogBookTabReturnLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogBookTabReturnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jDialogBookTabReturnLayout.createSequentialGroup()
                        .addGroup(jDialogBookTabReturnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparatorBookSearchHeader2)
                            .addComponent(lblBookTabReturnTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(13, 13, 13))
                    .addGroup(jDialogBookTabReturnLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jDialogBookTabReturnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblBookTabReturnMemTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblBookTabReturnBookTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jDialogBookTabReturnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jDialogBookTabReturnLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(btnBookTabReturnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnBookTabReturn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jDialogBookTabReturnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jTxtBookTabReturnBookInput, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                                .addComponent(jTxtBookTabReturnMemInput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jDialogBookTabReturnLayout.setVerticalGroup(
            jDialogBookTabReturnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogBookTabReturnLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblBookTabReturnTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparatorBookSearchHeader2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 39, Short.MAX_VALUE)
                .addGroup(jDialogBookTabReturnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBookTabReturnBookTxt)
                    .addComponent(jTxtBookTabReturnBookInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jDialogBookTabReturnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTxtBookTabReturnMemInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBookTabReturnMemTxt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jDialogBookTabReturnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBookTabReturn, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBookTabReturnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JoBas Library Application");
        setLocationByPlatform(true);
        setResizable(false);

        lblTitle.setFont(new java.awt.Font("Calibri", 1, 36)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(153, 153, 153));
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("The Library Application");

        jtabMainInterface.setToolTipText("");

        jPanelMembers.setToolTipText("");

        btnQueryMembers.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnQueryMembers.setText("Query Members");
        btnQueryMembers.setActionCommand("queryMembersNo");
        btnQueryMembers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQueryMembersActionPerformed(evt);
            }
        });

        jListMembersLoanedBooks.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Please select a member..." };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jListMembersLoanedBooks.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListMembersLoanedBooks.setToolTipText("");
        jListMembersLoanedBooks.setEnabled(false);
        jListMembersLoanedBooks.setFocusable(false);
        jListMembersLoanedBooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jListMembersLoanedBooksMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(jListMembersLoanedBooks);

        jListMembers.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jListMembers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListMembers.setFocusable(false);
        jListMembers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jListMembersMousePressed(evt);
            }
        });
        jScrollPane2.setViewportView(jListMembers);

        lblBooksLoaned.setText("Books Loaned");

        lblMembersJlistHead.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMembersJlistHead.setText("Members");

        btnDelMem.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnDelMem.setText("Del Memb");
        btnDelMem.setToolTipText("");
        btnDelMem.setActionCommand("queryMembersNo");
        btnDelMem.setEnabled(false);
        btnDelMem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelMemActionPerformed(evt);
            }
        });

        btnAddMem.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnAddMem.setText("Add Memb");
        btnAddMem.setToolTipText("");
        btnAddMem.setActionCommand("queryMembersNo");
        btnAddMem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddMemActionPerformed(evt);
            }
        });

        btnEditMem.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnEditMem.setText("Edit Memb");
        btnEditMem.setToolTipText("");
        btnEditMem.setActionCommand("queryMembersNo");
        btnEditMem.setEnabled(false);
        btnEditMem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditMemActionPerformed(evt);
            }
        });

        btnResetMembsFilter.setBackground(new java.awt.Color(255, 102, 102));
        btnResetMembsFilter.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        btnResetMembsFilter.setText("clear filter");
        btnResetMembsFilter.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        btnResetMembsFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetMembsFilterActionPerformed(evt);
            }
        });

        lblAvailableBooks.setText("Available Books");

        jListMembersAvailBooks.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Please select a member..." };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jListMembersAvailBooks.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListMembersAvailBooks.setToolTipText("");
        jListMembersAvailBooks.setEnabled(false);
        jListMembersAvailBooks.setFocusable(false);
        jListMembersAvailBooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jListMembersAvailBooksMousePressed(evt);
            }
        });
        jScrollPane5.setViewportView(jListMembersAvailBooks);

        btnIssueLoan.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnIssueLoan.setText("Issue Loan");
        btnIssueLoan.setToolTipText("");
        btnIssueLoan.setActionCommand("queryMembersNo");
        btnIssueLoan.setEnabled(false);
        btnIssueLoan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIssueLoanActionPerformed(evt);
            }
        });

        btnReturnBook.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnReturnBook.setText("Return Book");
        btnReturnBook.setToolTipText("");
        btnReturnBook.setActionCommand("queryMembersNo");
        btnReturnBook.setEnabled(false);
        btnReturnBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReturnBookActionPerformed(evt);
            }
        });

        lblNoMemQResultsTxt.setForeground(new java.awt.Color(255, 0, 0));
        lblNoMemQResultsTxt.setText("No Results returned!");

        lblLoanedBookCount.setForeground(new java.awt.Color(0, 204, 0));
        lblLoanedBookCount.setText("(0)");

        lblAvailBookCount.setText("(0)");

        javax.swing.GroupLayout jPanelMembersLayout = new javax.swing.GroupLayout(jPanelMembers);
        jPanelMembers.setLayout(jPanelMembersLayout);
        jPanelMembersLayout.setHorizontalGroup(
            jPanelMembersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMembersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelMembersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelMembersLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanelMembersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelMembersLayout.createSequentialGroup()
                                .addComponent(btnQueryMembers, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblNoMemQResultsTxt))
                            .addGroup(jPanelMembersLayout.createSequentialGroup()
                                .addComponent(btnAddMem)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnEditMem)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDelMem, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelMembersLayout.createSequentialGroup()
                        .addGroup(jPanelMembersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelMembersLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(lblMembersJlistHead)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnResetMembsFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanelMembersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelMembersLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(lblAvailableBooks)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblAvailBookCount)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnIssueLoan))
                            .addGroup(jPanelMembersLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanelMembersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE)
                                    .addComponent(jScrollPane1)
                                    .addGroup(jPanelMembersLayout.createSequentialGroup()
                                        .addGap(8, 8, 8)
                                        .addComponent(lblBooksLoaned)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblLoanedBookCount)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnReturnBook)))))
                        .addContainerGap())))
        );
        jPanelMembersLayout.setVerticalGroup(
            jPanelMembersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMembersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelMembersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnResetMembsFilter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelMembersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblMembersJlistHead)
                        .addComponent(lblBooksLoaned)
                        .addComponent(btnReturnBook)
                        .addComponent(lblLoanedBookCount)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelMembersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelMembersLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                        .addGroup(jPanelMembersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblAvailableBooks)
                            .addComponent(btnIssueLoan)
                            .addComponent(lblAvailBookCount))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2))
                .addGap(18, 18, 18)
                .addGroup(jPanelMembersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnQueryMembers, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNoMemQResultsTxt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelMembersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddMem, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditMem, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelMem, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jtabMainInterface.addTab("Members", jPanelMembers);

        jPanelBooks.setToolTipText("");

        btnQueryBooks.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnQueryBooks.setText("Query Books");
        btnQueryBooks.setActionCommand("queryMembersNo");
        btnQueryBooks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQueryBooksActionPerformed(evt);
            }
        });

        jListBooks.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jListBooks.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListBooks.setFocusable(false);
        jListBooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jListBooksMousePressed(evt);
            }
        });
        jScrollPane3.setViewportView(jListBooks);

        lblBooksJlistHead.setText("Book Collection...");

        btnAddBook.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnAddBook.setText("Add Book");
        btnAddBook.setToolTipText("");
        btnAddBook.setActionCommand("queryMembersNo");
        btnAddBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddBookActionPerformed(evt);
            }
        });

        btnDelBook.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnDelBook.setText("Del Book");
        btnDelBook.setToolTipText("");
        btnDelBook.setActionCommand("queryMembersNo");
        btnDelBook.setEnabled(false);
        btnDelBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelBookActionPerformed(evt);
            }
        });

        btnEditBook.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnEditBook.setText("Edit Book");
        btnEditBook.setToolTipText("");
        btnEditBook.setActionCommand("queryMembersNo");
        btnEditBook.setEnabled(false);
        btnEditBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditBookActionPerformed(evt);
            }
        });

        btnResetBooksFilter.setBackground(new java.awt.Color(255, 102, 102));
        btnResetBooksFilter.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        btnResetBooksFilter.setText("clear filter");
        btnResetBooksFilter.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        btnResetBooksFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetBooksFilterActionPerformed(evt);
            }
        });

        btnQueryFilterAvail.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnQueryFilterAvail.setText("Filter by availability");
        btnQueryFilterAvail.setActionCommand("queryMembersNo");
        btnQueryFilterAvail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQueryFilterAvailActionPerformed(evt);
            }
        });

        btnQueryFilterUnavail.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnQueryFilterUnavail.setText("Filter by unavailability");
        btnQueryFilterUnavail.setActionCommand("queryMembersNo");
        btnQueryFilterUnavail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQueryFilterUnavailActionPerformed(evt);
            }
        });

        btnIssueLoanBookTab.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnIssueLoanBookTab.setText("Issue Loan");
        btnIssueLoanBookTab.setToolTipText("");
        btnIssueLoanBookTab.setActionCommand("queryMembersNo");
        btnIssueLoanBookTab.setEnabled(false);
        btnIssueLoanBookTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIssueLoanBookTabActionPerformed(evt);
            }
        });

        btnReturnBookTab.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnReturnBookTab.setText("Return Book");
        btnReturnBookTab.setToolTipText("");
        btnReturnBookTab.setActionCommand("queryMembersNo");
        btnReturnBookTab.setEnabled(false);
        btnReturnBookTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReturnBookTabActionPerformed(evt);
            }
        });

        lblBookQResultsTxt.setForeground(new java.awt.Color(255, 0, 0));
        lblBookQResultsTxt.setText("No Results returned!");

        javax.swing.GroupLayout jPanelBooksLayout = new javax.swing.GroupLayout(jPanelBooks);
        jPanelBooks.setLayout(jPanelBooksLayout);
        jPanelBooksLayout.setHorizontalGroup(
            jPanelBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelBooksLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelBooksLayout.createSequentialGroup()
                        .addGroup(jPanelBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelBooksLayout.createSequentialGroup()
                                .addComponent(btnQueryBooks, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblBookQResultsTxt))
                            .addGroup(jPanelBooksLayout.createSequentialGroup()
                                .addComponent(btnAddBook)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnEditBook)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDelBook, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 269, Short.MAX_VALUE)
                        .addGroup(jPanelBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelBooksLayout.createSequentialGroup()
                                .addComponent(btnIssueLoanBookTab)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnReturnBookTab)
                                .addGap(7, 7, 7))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelBooksLayout.createSequentialGroup()
                                .addComponent(btnQueryFilterAvail, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnQueryFilterUnavail))))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelBooksLayout.createSequentialGroup()
                        .addComponent(lblBooksJlistHead, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 648, Short.MAX_VALUE)
                        .addComponent(btnResetBooksFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(36, 36, 36))
        );
        jPanelBooksLayout.setVerticalGroup(
            jPanelBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelBooksLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanelBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnResetBooksFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBooksJlistHead))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanelBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelBooksLayout.createSequentialGroup()
                        .addGroup(jPanelBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnQueryBooks, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnQueryFilterAvail, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnQueryFilterUnavail, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblBookQResultsTxt))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddBook, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEditBook, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDelBook, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelBooksLayout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(jPanelBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnReturnBookTab, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnIssueLoanBookTab, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(25, 25, 25))
        );

        jtabMainInterface.addTab("Books", jPanelBooks);

        btnSaveChanges.setBackground(new java.awt.Color(153, 255, 153));
        btnSaveChanges.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnSaveChanges.setText("Save Changes");
        btnSaveChanges.setActionCommand("queryMembersNo");
        btnSaveChanges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveChangesActionPerformed(evt);
            }
        });

        btnQuit.setBackground(new java.awt.Color(255, 102, 102));
        btnQuit.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnQuit.setText("Quit");
        btnQuit.setActionCommand("queryMembersNo");
        btnQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitActionPerformed(evt);
            }
        });

        lblChangesSaved.setForeground(new java.awt.Color(0, 204, 0));
        lblChangesSaved.setText("Changes Saved");

        lblMainMemberCntTxt.setText("Member Count:");

        lblMainBookCntTxt.setText("Book Count:");

        lblMainMemberCnt.setText("Num");

        lblMainBookCnt.setText("Num");

        javax.swing.GroupLayout jPanelContainerLayout = new javax.swing.GroupLayout(jPanelContainer);
        jPanelContainer.setLayout(jPanelContainerLayout);
        jPanelContainerLayout.setHorizontalGroup(
            jPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelContainerLayout.createSequentialGroup()
                        .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelContainerLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jtabMainInterface, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelContainerLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(lblMainMemberCntTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblMainMemberCnt)
                        .addGap(23, 23, 23)
                        .addComponent(lblMainBookCntTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblMainBookCnt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblChangesSaved)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSaveChanges)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuit, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30))))
        );
        jPanelContainerLayout.setVerticalGroup(
            jPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jtabMainInterface, javax.swing.GroupLayout.PREFERRED_SIZE, 451, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblMainMemberCntTxt)
                        .addComponent(lblMainBookCntTxt)
                        .addComponent(lblMainMemberCnt)
                        .addComponent(lblMainBookCnt))
                    .addGroup(jPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnSaveChanges, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnQuit, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblChangesSaved)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnQueryMembersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQueryMembersActionPerformed
        showMemSearchGUI();
    }//GEN-LAST:event_btnQueryMembersActionPerformed

    private void btnSaveChangesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveChangesActionPerformed
        // TODO add your handling code here:
        lblChangesSaved.setVisible(true);
        saveAll();
        
    }//GEN-LAST:event_btnSaveChangesActionPerformed

    private void btnQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitActionPerformed
        // TODO add your handling code here:
        
        showQuitConfGUI();
    }//GEN-LAST:event_btnQuitActionPerformed

    private void btnDelMemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelMemActionPerformed
        // TODO add your handling code here:
        
        // HERE
        showMemCRUDGUI("delete");
    }//GEN-LAST:event_btnDelMemActionPerformed

    private void btnAddMemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddMemActionPerformed
        // TODO add your handling code here:
        showMemCRUDGUI("new");
    }//GEN-LAST:event_btnAddMemActionPerformed

    private void btnEditMemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditMemActionPerformed
        // TODO add your handling code here:
        
        showMemCRUDGUI("edit");
    }//GEN-LAST:event_btnEditMemActionPerformed

    private void btnEditBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditBookActionPerformed
        // TODO add your handling code here:
        showBookCRUDGUI("edit");
    }//GEN-LAST:event_btnEditBookActionPerformed

    private void btnDelBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelBookActionPerformed
        // TODO add your handling code here:
        showBookCRUDGUI("delete");
    }//GEN-LAST:event_btnDelBookActionPerformed

    private void btnAddBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBookActionPerformed
        // TODO add your handling code here:
        showBookCRUDGUI("new");
    }//GEN-LAST:event_btnAddBookActionPerformed

    private void btnQueryBooksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQueryBooksActionPerformed
        // TODO add your handling code here:
        showBookSearchGUI();
    }//GEN-LAST:event_btnQueryBooksActionPerformed

    private void btnQuitConfJQActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitConfJQActionPerformed
        // TODO add your handling code here:
         System.exit(0);
    }//GEN-LAST:event_btnQuitConfJQActionPerformed

    private void btnQuitConfSQActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitConfSQActionPerformed
        // TODO add your handling code here:
        saveAll();
        System.exit(0);
    }//GEN-LAST:event_btnQuitConfSQActionPerformed

    private void btnResetMembsFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetMembsFilterActionPerformed
        // TODO add your handling code here:
        hideFilterBtn(this.btnResetMembsFilter);
        clearSelectedMember();
        clearSelectedBook();
        refreshMemBookJLists();
    }//GEN-LAST:event_btnResetMembsFilterActionPerformed

    private void btnResetBooksFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetBooksFilterActionPerformed
        // TODO add your handling code here:
        hideFilterBtn(this.btnResetBooksFilter);
        //clearSelectedMember();
        clearSelectedBook();
        refreshBooksJList(null);
    }//GEN-LAST:event_btnResetBooksFilterActionPerformed

    private void jDialogBookSearchWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jDialogBookSearchWindowActivated
        // TODO add your handling code here
        jDialogBookSearch.setAlwaysOnTop(true);
    }//GEN-LAST:event_jDialogBookSearchWindowActivated

    private void btnIssueLoanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIssueLoanActionPerformed
        // TODO add your handling code here:
        selectedMember.borrowBook(selectedBook);
        setGUITxt();
        refreshMemBookJLists();
        refreshBooksJList(null);
        setSelectedMember();
    }//GEN-LAST:event_btnIssueLoanActionPerformed

    private void btnReturnBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReturnBookActionPerformed
        // TODO add your handling code here:
        selectedMember.returnBook(selectedBook);
        setGUITxt();
        refreshMemBookJLists();
        refreshBooksJList(null);
        setSelectedMember();
    }//GEN-LAST:event_btnReturnBookActionPerformed

    private void btnQueryFilterAvailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQueryFilterAvailActionPerformed
        // TODO add your handling code here:       
        SetOfBooks quickFiltUnavail;
        quickFiltUnavail = theBooks.getAvailableBooks(theBooks);
        refreshBooksJList(quickFiltUnavail);
        btnResetBooksFilter.setVisible(true);
        clearSelectedBook();
    }//GEN-LAST:event_btnQueryFilterAvailActionPerformed

    private void btnQueryFilterUnavailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQueryFilterUnavailActionPerformed
        // TODO add your handling code here:
        SetOfBooks quickFiltAvail;
        quickFiltAvail = theBooks.getLoanedBooks(theBooks);
        refreshBooksJList(quickFiltAvail);
        btnResetBooksFilter.setVisible(true);
        clearSelectedBook();
    }//GEN-LAST:event_btnQueryFilterUnavailActionPerformed

    private void btnQuitConfYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitConfYesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnQuitConfYesActionPerformed

    private void btnQuitConfNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitConfNoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnQuitConfNoActionPerformed

    private void btnBookFormCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookFormCancelActionPerformed
        // TODO add your handling code here:
        jDialogCRUDBookForm.setVisible(false);
    }//GEN-LAST:event_btnBookFormCancelActionPerformed

    private void btnBookFormApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookFormApplyActionPerformed
        // TODO add your handling code here:
        
        String btnTxt = btnBookFormApply.getText();
        String bookTitleInput = jTxtBookTitleInput.getText();
        String bookAuthInput = jTxtBookAuthorInput.getText();
        //System.out.println(bookAuthInput);
        //System.out.println(!bookAuthInput.isEmpty());
        String ISBNInput = jTxtBookISBNInput.getText();
        if(!bookTitleInput.trim().isEmpty() && !bookAuthInput.trim().isEmpty() && !ISBNInput.trim().isEmpty()){
            try{
                int ISBNNo = parseInt(ISBNInput.replaceAll("\\D+",""));
                if (btnTxt.equals("Add")){ // btn txt == add
                    Book book = new Book(theBooks.lastElement().getBookAccNo()+1,bookTitleInput, bookAuthInput, ISBNNo, null);
                    theBooks.addBook(book);
                }else if (btnTxt.equals("Save")){ // btn txt == edit
                    selectedBook.setTitle(bookTitleInput);
                    selectedBook.setAuthor(bookAuthInput);
                    selectedBook.setISBNNumber(ISBNNo);
                }else{ // btn txt == delete
                    theBooks.removeBook(selectedBook);
                }

                setGUITxt();

                jDialogCRUDBookForm.setVisible(false);
                clearSelectedBook();
                refreshBooksJList(null);
            }
            catch(Exception e){
                jTxtBookISBNInput.setText("");
            }
        }
            
        
    }//GEN-LAST:event_btnBookFormApplyActionPerformed

    private void btnMemFormApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMemFormApplyActionPerformed
        // TODO add your handling code here:
        String btnTxt = btnMemFormApply.getText();
        String memName = new String();

        memName = jTxtMemNameInput.getText();
        if(!btnTxt.isEmpty() && !memName.trim().isEmpty()){
            if (btnTxt.equals("Add")){ // btn txt == add
                Member member = new Member(theMembers.lastElement().getMemberNumber()+1,memName, null);
                theMembers.addMember(member);
            }else if (btnTxt.equals("Save")){ // btn txt == edit
                selectedMember.setName(memName);
            }else{ // btn txt == delete
                // Need to stop this happening if user has books on loan
                theMembers.removeMember(selectedMember);
            }
        
            setGUITxt();

            jDialogCRUDMemForm.setVisible(false);
            clearSelectedMember();
            refreshMembersJList(null);
        }
    }//GEN-LAST:event_btnMemFormApplyActionPerformed

    private void btnMemFormCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMemFormCancelActionPerformed
        // TODO add your handling code here:
        jDialogCRUDMemForm.setVisible(false);
    }//GEN-LAST:event_btnMemFormCancelActionPerformed

    private void btnMemSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMemSearchActionPerformed
        // TODO add your handling code here:
        filterMembers();        
    }//GEN-LAST:event_btnMemSearchActionPerformed

    private void jDialogMemSearchWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jDialogMemSearchWindowActivated
        // TODO add your handling code here:
    }//GEN-LAST:event_jDialogMemSearchWindowActivated

    private void btnBookSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookSearchActionPerformed
        // TODO add your handling code here:
        filterBooks();
    }//GEN-LAST:event_btnBookSearchActionPerformed

    private void btnIssueLoanBookTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIssueLoanBookTabActionPerformed
        // TODO add your handling code here:
        initiateBookTabLoan();
    }//GEN-LAST:event_btnIssueLoanBookTabActionPerformed

    private void btnReturnBookTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReturnBookTabActionPerformed
        // TODO add your handling code here:
        initiateBookTabReturn();
    }//GEN-LAST:event_btnReturnBookTabActionPerformed

    private void jListMembersMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListMembersMousePressed
        // TODO add your handling code here:
        setSelectedMember();
        refreshMemBookJLists();
    }//GEN-LAST:event_jListMembersMousePressed

    private void jListMembersLoanedBooksMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListMembersLoanedBooksMousePressed
        // TODO add your handling code here:
        try{
            // get selected value, store as a string
            String sBookAccNo = jListMembersLoanedBooks.getSelectedValue().toString();
            // get everything after last , char and trim trailing spaces, leaving a number
            sBookAccNo = sBookAccNo.substring(sBookAccNo.lastIndexOf("~")+1).trim();

            // parse the string into an int
            selectedBook = theBooks.findBookFromAccNumber(parseInt(sBookAccNo)).firstElement();
            jListMembersAvailBooks.clearSelection(); // clear selection in books loaned
            btnIssueLoan.setEnabled(false);
            btnReturnBook.setEnabled(true);
        }catch(Exception e){
            // error handling space
        }
    }//GEN-LAST:event_jListMembersLoanedBooksMousePressed

    private void jListMembersAvailBooksMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListMembersAvailBooksMousePressed
        // TODO add your handling code here:
        // get selected value, store as a string
        try{
            String sBookAccNo = jListMembersAvailBooks.getSelectedValue().toString();
            // get everything after last , char and trim trailing spaces, leaving a number
            sBookAccNo = sBookAccNo.substring(sBookAccNo.lastIndexOf("~")+1).trim();

            // parse the string into an int
            selectedBook = theBooks.findBookFromAccNumber(parseInt(sBookAccNo)).firstElement();
            jListMembersLoanedBooks.clearSelection(); // clear selection in books loaned
            btnReturnBook.setEnabled(false);
            btnIssueLoan.setEnabled(true);  
        }catch(Exception e){
            // error handling space
        }

    }//GEN-LAST:event_jListMembersAvailBooksMousePressed

    private void jListBooksMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListBooksMousePressed
        // TODO add your handling code here:
        
        setSelectedBook();
    }//GEN-LAST:event_jListBooksMousePressed

    private void btnBookTabLoanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookTabLoanActionPerformed
        // TODO add your handling code here:
        //LOAN CODE HERE
        String jcomboSelMem = jComboBookTabLoanMem.getSelectedItem().toString();
        selectedMember = theMembers.getMemberFromNumber(parseInt(
                jcomboSelMem.replaceAll("\\D+","")));
        selectedMember.borrowBook(selectedBook);
        //setGUITxt();
        refreshMemBookJLists();
        refreshBooksJList(null);

        hideFilterBtn(btnResetBooksFilter);
        clearSelectedMember();
        clearSelectedBook();
        jDialogBookTabLoan.setVisible(false);
    }//GEN-LAST:event_btnBookTabLoanActionPerformed

    private void btnBookTabLoanCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookTabLoanCancelActionPerformed
        // TODO add your handling code here:
        jDialogBookTabLoan.setVisible(false);
    }//GEN-LAST:event_btnBookTabLoanCancelActionPerformed

    private void btnBookTabReturnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookTabReturnActionPerformed
        // TODO add your handling code here:
        
        //RETURN CODE HERE
        //jTxtBookTabReturnMemInput.setText(selectedBook.getBorrower().getName());
        selectedBook.getBorrower().returnBook(selectedBook);

        //setGUITxt();
        refreshMemBookJLists();
        refreshBooksJList(null);

        hideFilterBtn(btnResetBooksFilter);
        clearSelectedMember();
        clearSelectedBook();
        
        jDialogBookTabReturn.setVisible(false);
    }//GEN-LAST:event_btnBookTabReturnActionPerformed

    private void btnBookTabReturnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookTabReturnCancelActionPerformed
        // TODO add your handling code here:
        jDialogBookTabReturn.setVisible(false);
    }//GEN-LAST:event_btnBookTabReturnCancelActionPerformed

    private void jComboBookTabLoanMemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBookTabLoanMemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBookTabLoanMemActionPerformed

    private void jComboBookTabLoanMemItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBookTabLoanMemItemStateChanged
        // TODO add your handling code here:
        
        Member tempMem = selectedMember;    //temp storage until action is complete
        clearSelectedMember();
        selectedMember = (Member) jComboBookTabLoanMem.getSelectedItem();
        
        if (selectedMember != null){
            SetOfBooks tempBookCol = new SetOfBooks();

            for(Book book : theBooks) {
                if (book.getBorrower() != null){
                    if(selectedMember.getMemberNumber() == book.getBorrower().getMemberNumber()){
                        tempBookCol.addBook(book);
                    }
                }        
            }
            if(tempBookCol.size()>2){
                btnBookTabLoan.setEnabled(false);
            }else{
                btnBookTabLoan.setEnabled(true);
            }
            selectedMember = tempMem;
        }
        
    }//GEN-LAST:event_jComboBookTabLoanMemItemStateChanged

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LibraryGUI().setVisible(true);
            }

        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddBook;
    private javax.swing.JButton btnAddMem;
    private javax.swing.JButton btnBookFormApply;
    private javax.swing.JButton btnBookFormCancel;
    private javax.swing.JButton btnBookSearch;
    private javax.swing.JButton btnBookTabLoan;
    private javax.swing.JButton btnBookTabLoanCancel;
    private javax.swing.JButton btnBookTabReturn;
    private javax.swing.JButton btnBookTabReturnCancel;
    private javax.swing.JButton btnDelBook;
    private javax.swing.JButton btnDelMem;
    private javax.swing.JButton btnEditBook;
    private javax.swing.JButton btnEditMem;
    private javax.swing.JButton btnIssueLoan;
    private javax.swing.JButton btnIssueLoanBookTab;
    private javax.swing.JButton btnMemFormApply;
    private javax.swing.JButton btnMemFormCancel;
    private javax.swing.JButton btnMemSearch;
    private javax.swing.JButton btnQueryBooks;
    private javax.swing.JButton btnQueryFilterAvail;
    private javax.swing.JButton btnQueryFilterUnavail;
    private javax.swing.JButton btnQueryMembers;
    private javax.swing.JButton btnQuit;
    private javax.swing.JButton btnQuitConfJQ;
    private javax.swing.JButton btnQuitConfNo;
    private javax.swing.JButton btnQuitConfSQ;
    private javax.swing.JButton btnQuitConfYes;
    private javax.swing.JButton btnResetBooksFilter;
    private javax.swing.JButton btnResetMembsFilter;
    private javax.swing.JButton btnReturnBook;
    private javax.swing.JButton btnReturnBookTab;
    private javax.swing.JButton btnSaveChanges;
    private javax.swing.JComboBox jComboBookFilter;
    private javax.swing.JComboBox jComboBookTabLoanMem;
    private javax.swing.JComboBox jComboMemFilter;
    private javax.swing.JDialog jDialogBookSearch;
    private javax.swing.JDialog jDialogBookTabLoan;
    private javax.swing.JDialog jDialogBookTabReturn;
    private javax.swing.JDialog jDialogCRUDBookForm;
    private javax.swing.JDialog jDialogCRUDMemForm;
    private javax.swing.JDialog jDialogDelConf;
    private javax.swing.JDialog jDialogMemSearch;
    private javax.swing.JDialog jDialogQuitConf;
    private javax.swing.JList jListBooks;
    private javax.swing.JList jListMembers;
    private javax.swing.JList jListMembersAvailBooks;
    private javax.swing.JList jListMembersLoanedBooks;
    private javax.swing.JPanel jPanelBooks;
    private javax.swing.JPanel jPanelContainer;
    private javax.swing.JPanel jPanelMembers;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparatorBookFormHeader;
    private javax.swing.JSeparator jSeparatorBookSearchHeader;
    private javax.swing.JSeparator jSeparatorBookSearchHeader1;
    private javax.swing.JSeparator jSeparatorBookSearchHeader2;
    private javax.swing.JSeparator jSeparatorDelConfHeader;
    private javax.swing.JSeparator jSeparatorMemFormHeader;
    private javax.swing.JSeparator jSeparatorMemSearchHeader;
    private javax.swing.JSeparator jSeparatorQuitConfHeader;
    private javax.swing.JTextPane jTxtBookAccNoInput;
    private javax.swing.JTextPane jTxtBookAuthorInput;
    private javax.swing.JTextPane jTxtBookISBNInput;
    private javax.swing.JTextField jTxtBookSearchInput;
    private javax.swing.JTextField jTxtBookTabLoanBookInput;
    private javax.swing.JTextField jTxtBookTabReturnBookInput;
    private javax.swing.JTextField jTxtBookTabReturnMemInput;
    private javax.swing.JTextPane jTxtBookTitleInput;
    private javax.swing.JTextPane jTxtMemIDInput;
    private javax.swing.JTextPane jTxtMemNameInput;
    private javax.swing.JTextField jTxtMemSearchInput;
    private javax.swing.JTabbedPane jtabMainInterface;
    private javax.swing.JLabel lblAvailBookCount;
    private javax.swing.JLabel lblAvailableBooks;
    private javax.swing.JLabel lblBookAccNoTxt;
    private javax.swing.JLabel lblBookAuthorTxt;
    private javax.swing.JLabel lblBookCountTxt;
    private javax.swing.JLabel lblBookCountVal;
    private javax.swing.JLabel lblBookFormHeader;
    private javax.swing.JLabel lblBookISBNTxt;
    private javax.swing.JLabel lblBookQResultsTxt;
    private javax.swing.JLabel lblBookSearchFilterTxt;
    private javax.swing.JLabel lblBookSearchInputTxt;
    private javax.swing.JLabel lblBookSearchTitle;
    private javax.swing.JLabel lblBookTabLoanBookTxt;
    private javax.swing.JLabel lblBookTabLoanMemTxt;
    private javax.swing.JLabel lblBookTabLoanTitle;
    private javax.swing.JLabel lblBookTabReturnBookTxt;
    private javax.swing.JLabel lblBookTabReturnMemTxt;
    private javax.swing.JLabel lblBookTabReturnTitle;
    private javax.swing.JLabel lblBookTitleTxt;
    private javax.swing.JLabel lblBooksJlistHead;
    private javax.swing.JLabel lblBooksLoaned;
    private javax.swing.JLabel lblChangesSaved;
    private javax.swing.JLabel lblDelConfTitle;
    private javax.swing.JLabel lblLoanedBookCount;
    private javax.swing.JLabel lblMainBookCnt;
    private javax.swing.JLabel lblMainBookCntTxt;
    private javax.swing.JLabel lblMainMemberCnt;
    private javax.swing.JLabel lblMainMemberCntTxt;
    private javax.swing.JLabel lblMemFormTitle;
    private javax.swing.JLabel lblMemIDInputTxt;
    private javax.swing.JLabel lblMemNameInputTxt;
    private javax.swing.JLabel lblMemSearchFilterTxt;
    private javax.swing.JLabel lblMemSearchInputTxt;
    private javax.swing.JLabel lblMemSearchTitle;
    private javax.swing.JLabel lblMembersJlistHead;
    private javax.swing.JLabel lblNoMemQResultsTxt;
    private javax.swing.JLabel lblQuitConfTitle;
    private javax.swing.JLabel lblTitle;
    // End of variables declaration//GEN-END:variables

}
