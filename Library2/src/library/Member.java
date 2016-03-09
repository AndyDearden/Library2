package library;

import java.io.Serializable;

public class Member implements Serializable
{
    private String name;
    private int memberNumber;
    private SetOfBooks currentLoans = new SetOfBooks();
    private static int memberCount = 0;
    
    public Member(Integer memID, String aName, SetOfBooks mBooks)
    {
        name = aName;
        memberNumber = memID;
                
        if (mBooks==null){
            currentLoans = new SetOfBooks();
        }else{
            currentLoans = mBooks;
        }
        
    }

    public void borrowBook(Book aBook)
    {
        aBook.setCurrentBorrower(this);
        currentLoans.addBook(aBook);
    }

    public void returnBook(Book aBook)
    {
        currentLoans.removeBook(aBook);
        aBook.setCurrentBorrower(null);
    }
    
    public String toString()
    {
        return Integer.toString(memberNumber) + " ~ " + name;
    }
 
    public SetOfBooks getBooksOnLoan()
    {
        return currentLoans;
    }
    
    public void returnAllBOL(){ // return Books on loan for serialization
        currentLoans.removeAllElements();
    }
    
    public String getName(){
        return name;
    }
    
    public void setName(String nName){
        name = nName;
    }
   
    public int getMemberNumber(){
        return memberNumber;
    }
}

