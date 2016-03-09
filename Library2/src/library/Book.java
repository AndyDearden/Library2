package library;

import java.io.Serializable;

public class Book  implements Serializable
{

    private int iSBNNumber;
    private String title;
    private String author;
    private int accessionNumber = 0;
    private Member borrower = null;
    private static int bookCount = 0;

    public Book(Integer accNo, String bName, String bAuthor, int bISBNNumber, Member cBookBor)
    {
        title = bName;
        author = bAuthor;
        iSBNNumber = bISBNNumber;
        //accessionNumber = bookCount++;
        accessionNumber = accNo;
        borrower = cBookBor;
    }
    
    public Member getBorrower() 
    {
        return borrower;
    }

    public void setCurrentBorrower(Member theBorrower) 
    {
        borrower = theBorrower;
    }

    public String toString()
    {
        return title + " ~ " + author + " ~ " + accessionNumber;
    }

    public boolean isOnLoan()
    {
        return borrower != null;
    }
    
    public String getTitle(){
        return title;
    }
    
    public void setTitle(String s){
        title =  s;
    }
    
    public int getISBNNumber(){
        return iSBNNumber;
    }
    
    public void setISBNNumber(int i){
        iSBNNumber = i;
    }
    
    public int getBookAccNo(){
        return accessionNumber;
    }
    
    public String getAuthor(){
        return author;
    }
    public void setAuthor(String s){
        author = s;
    }
    
}
