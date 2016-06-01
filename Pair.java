/*
Class to represent a natural ordering of paired items
*/
public class Pair<L,R>
{
    L left;
    R right;
    
    public Pair(L left, R right)
    {
        this.left = left;
        this.right = right;
    }
    
    public L getLeft()
    {
        return left;
    }
    
    public R getRight()
    {
        return right;
    }
    
    @Override
    public String toString()
    {
        return right.toString();
    }
}
