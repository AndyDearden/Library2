package library;

import java.io.Serializable;
import java.util.Vector;

public class SetOfMembers extends Vector<Member> implements Serializable
{
    void addMember(Member aMember) 
    {
        super.add(aMember);
    }
    
    Member getMemberFromName(String aMember) 
    {
        for(int x = 0; x < super.size(); x++)
        {
            if (super.get(x).getName().contains(aMember))
            {
                return super.get(x);
            }
        }
        return null;
    }
    
    Member getMemberFromNumber(int aMember) 
    {
        for(int x = 0; x < super.size(); x++)
        {
            if (aMember == super.get(x).getMemberNumber())
            {
                return super.get(x);
            }
        }
        return null;
    }
    
    public SetOfMembers getMembersFromSearch(String memName){
        SetOfMembers qResult = new SetOfMembers();
        for(int x = 0; x < super.size(); x++)
        {
            if (super.get(x).getName().toUpperCase().contains(
                    memName.toUpperCase()))
            {
                qResult.addMember(super.get(x));
            }
        }
        return qResult;
    }
    
    public SetOfMembers getMembersFromSearch(int memID) {
        SetOfMembers qResult = new SetOfMembers();
        for(int x = 0; x < super.size(); x++)
        {
            if (memID == (super.get(x).getMemberNumber()))
            {
                qResult.addMember(super.get(x));
            }
        }
        return qResult;
    }

    void removeMember(Member aMember) 
    {
        super.remove(aMember);
    }
}