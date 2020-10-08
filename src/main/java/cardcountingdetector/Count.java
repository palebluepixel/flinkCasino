package cardcountingdetector;

// Wrapper for a count because as far as I can tell everything needs a key
public class Count {
    private int count;

    public Count(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public int getKey() {
        return 1;
    }
}
