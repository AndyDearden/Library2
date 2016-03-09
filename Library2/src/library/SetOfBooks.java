/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package library;

import java.io.Serializable;
import java.util.Vector;
/**
 *
 * @author Kutoma
 */
public class SetOfBooks extends Vector<Book>  implements Serializable
{
    public SetOfBooks()
    {
        super();
    }
    
    public void addBook(Book aBook)
    {
        super.add(aBook);
    }    
    
    public SetOfBooks findBookByAuthor(String aBook)
    {
        SetOfBooks qResult = new SetOfBooks();
        for(int x = 0; x < super.size(); x++)
        {
            if (super.get(x).getAuthor().toUpperCase().contains(
                    aBook.toUpperCase()))
            {
                qResult.addBook(super.get(x));
            }
        }
        return qResult;
    }
    
    public SetOfBooks findBookFromTitle(String aBook)
    {
        SetOfBooks qResult = new SetOfBooks();
        
        return qResult;
    }
    
    public SetOfBooks findBookFromAccNumber(int accNo)
    {
        SetOfBooks qResult = new SetOfBooks();
       
        return qResult;
    }
    
    public SetOfBooks findBookFromISBN(int aISBN)
    {
       SetOfBooks qResult = new SetOfBooks();
       
        return qResult;
    }
    
    public void removeBook(Book aBook)
    {
        super.remove(aBook);
    }
    
    public SetOfBooks getLoanedBooks(SetOfBooks theBooks){
        SetOfBooks lBooks = new SetOfBooks();
        for(Book book: theBooks){
            if(book.isOnLoan()){
                lBooks.add(book);
                
            }
        }
        return lBooks;
    }   
    public SetOfBooks getAvailableBooks(SetOfBooks theBooks){
        SetOfBooks aBooks = new SetOfBooks(); // available books
        for(Book book: theBooks){
            if(!book.isOnLoan()){
                aBooks.add(book);
            }
        }
        return aBooks;
    }
    
}