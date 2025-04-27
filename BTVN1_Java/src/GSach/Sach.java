package GSach;

public class Sach extends Tailieu{
    private int NamXB;
    private int GiaB;
    
    
    public Sach(){}
    public Sach(int NamXB, int GiaB) {
        this.NamXB = NamXB;
        this.GiaB = GiaB;
    }

    public Sach(String MaS, String TenS,int NamXB, int GiaB) {
        super(MaS, TenS);
        this.NamXB = NamXB;
        this.GiaB = GiaB;
    }

    public int getNamXB() {
        return NamXB;
    }

    public int getGiaB() {
        return GiaB;
    }

    public void setNamXB(int NamXB) {
        this.NamXB = NamXB;
    }

    public void setGiaB(int GiaB) {
        this.GiaB = GiaB;
    }
    
    
    @Override
    public double Thanhtien(){
        if(this.NamXB<2015){
            return (this.GiaB * 0.85);
        }
        else{
            return (this.GiaB * 0.95);
        }
    }
}
