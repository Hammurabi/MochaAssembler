package mochaxx;

public class Tuple<I, J>
{
    private I i;
    private J j;

    public Tuple(I i, J j)
    {
        this.i = i;
        this.j = j;
    }

    public synchronized I getI()
    {
        return i;
    }

    public synchronized void setI(I i)
    {
        this.i = i;
    }

    public synchronized J getJ()
    {
        return j;
    }

    public synchronized void setJ(J j)
    {
        this.j = j;
    }

    @Override
    public String toString()
    {
        return i + " " + j;
    }
}