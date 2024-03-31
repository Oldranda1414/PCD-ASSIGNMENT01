package pcd.ass01.simengineconcurrent;

public interface Buffer<I> {

    public void put(String key, I item) throws InterruptedException;
    
    public I get(String key) throws InterruptedException;
    
    public boolean isEmpty();
}
