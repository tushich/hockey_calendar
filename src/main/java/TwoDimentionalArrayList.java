import java.util.ArrayList;

class TwoDimentionalArrayList<T> extends ArrayList<ArrayList<T>> {
    public Object getCellValue(int index, int index2)
    {
        return this.get(index).get(index2);
    }

    public void addToInnerArray(int index, int index2, T element)
    {
        while (index >= this.size())
        {
            this.add(new ArrayList<T>());
        }

        ArrayList<T> inner = this.get(index);
        while (index2 >= inner.size())
        {
            inner.add(null);
        }

        inner.set(index2, element);
    }
}