import java.util.ArrayList;
import java.util.Random;

public class User {
    private ArrayList<Integer> duplicates;
    private ArrayList<Integer> missing;
    
    public User()
    {
        this.duplicates = new ArrayList<Integer>();
        this.missing = new ArrayList<Integer>();
        ArrayList<Integer> stickers = new ArrayList<Integer>();
        Random rnd = new Random();
        
        for(int i = 0; i < 99; i++)
            stickers.add(i + 1);
        
        int numMissing = rnd.nextInt(98) + 1; // 98 - 1
        for(int i = 0; i < numMissing; i++)
        {
            int index = rnd.nextInt(stickers.size());
            this.missing.add(stickers.get(index));
            stickers.remove(index);
        }
        
        int numDuplicates = rnd.nextInt(99 - numMissing) + 1;
        for(int i = 0; i < numDuplicates; i++)
        {
            int index = rnd.nextInt(stickers.size());
            this.duplicates.add(stickers.get(index));
            stickers.remove(index);
        }
    }
    
    public int getDuplicate(int index)
    {
        return this.duplicates.get(index);
    }
    
    public int getMissing(int index)
    {
        return this.missing.get(index);
    }
    
    public void removeDuplicate(Integer val)
    {
        this.duplicates.remove(val);
    }
    
    public void removeMissing(Integer val)
    {
        this.missing.remove(val);
    }
    
    public String getDuplicatesString()
    {
        String stringOfDuplicates = "";
        for(int el : this.duplicates)
        {
            stringOfDuplicates += el + ",";
        }
        
        stringOfDuplicates = stringOfDuplicates.substring(0, stringOfDuplicates.length() - 1);
        return stringOfDuplicates;
    }
    
    public String getMissingString()
    {
        String stringOfMissing = "";
        for(int el : this.missing)
        {
            stringOfMissing += el + ",";
        }
        
        stringOfMissing = stringOfMissing.substring(0, stringOfMissing.length() - 1);
        return stringOfMissing;
    }
    
    public int getNumOfDuplicates()
    {
        return this.duplicates.size();
    }
    
    public int getNumOfMissing()
    {
        return this.missing.size();
    }
}
