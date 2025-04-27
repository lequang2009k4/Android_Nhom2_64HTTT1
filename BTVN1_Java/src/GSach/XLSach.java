package GSach;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
public class XLSach {
    private Connection conn;
    public Connection getCon(){
        String url="jdbc:sqlserver://localhost:1433;databaseName=DLSach;encrypt=true;trustServerCertificate=true";
        String user="sa";
        String password="123456789";
        try{
            conn= DriverManager.getConnection(url,user,password);
            System.out.println("Kết nối thàng công");
        }
        catch(SQLException e){
            e.getStackTrace();
        }
        return conn;
    }
    public List<Sach> getSA(){
        List<Sach> danhsachSa = new ArrayList<>();
        String sql="SELECT * FROM [tbSach]";
        try(PreparedStatement pst = conn.prepareStatement(sql)){
            try(ResultSet rs = pst.executeQuery()){
                while(rs.next()){
                    Sach sh=new Sach(
                            rs.getString("MaS"),
                            rs.getString("TenS"),
                            rs.getInt("NamXB"),
                            rs.getInt("GiaB")
                    );
                    danhsachSa.add(sh);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Loi tim kiem");
        }
        return danhsachSa;
    }
    public boolean deleteSA(int NamXB){
        String sql = " Delete from [dbo].[tbSach] where [NamXB] = ?";
        try(PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, NamXB);
            
            int Rows = pst.executeUpdate();
            return Rows>0;
        } catch (Exception e) {
            System.out.println("Lỗi"+ e.getMessage());
            return false;
        }
    }
    public boolean insertSA(Sach s){
        String sql = "insert into tbSach values (?,?,?,?)";
        try(PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1,s.getMaS());
            pst.setString(2, s.getTenS());
            pst.setInt(3, s.getNamXB());
            pst.setInt(4, s.getGiaB());
            int Rows = pst.executeUpdate();
            return Rows>0;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean updateSA(Sach s){
        String sql="update tbSach set TenS=?, NamXB=?,GiaB=? where MaS=?";
        try(PreparedStatement pst=conn.prepareStatement(sql) ) {
            pst.setString(1, s.getTenS());
            pst.setInt(2, s.getNamXB());
            pst.setInt(3, s.getGiaB());
            pst.setString(4, s.getMaS());
            int Rows=pst.executeUpdate();
            return Rows>0;
        } catch (Exception e) {
            return false;
        }
    }
    public List<Sach> searchSA(String MaS){
        List<Sach> danhsachSa = new ArrayList<>();
        String sql="SELECT * FROM [tbSach] where MaS = ?";
        try(PreparedStatement pst = conn.prepareStatement(sql)){
            pst.setString(1, MaS);
            try(ResultSet rs = pst.executeQuery()){
                while(rs.next()){
                    Sach sh=new Sach(
                            rs.getString("MaS"),
                            rs.getString("TenS"),
                            rs.getInt("NamXB"),
                            rs.getInt("GiaB")
                    );
                    danhsachSa.add(sh);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Loi tim kiem");
        }
        return danhsachSa;
    }
}
