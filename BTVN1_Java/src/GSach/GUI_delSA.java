package GSach;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;


public final class GUI_delSA extends JFrame implements ActionListener{
    private int currentRow=-1;
    private DefaultTableModel tablemodel= new DefaultTableModel();
    XLSach xl=new XLSach();
    
    public void loadData(DefaultTableModel tableModel){
        try{
            tableModel.setRowCount(0);            
            List<Sach> sach = xl.getSA();
            for(Sach s:sach){
                    Object[] RowsData = {
                        s.getMaS(),
                        s.getTenS(), 
                        s.getNamXB(),
                        s.getGiaB()
                    };
                    tableModel.addRow(RowsData);
            }
            tableModel.fireTableDataChanged();
        }
        catch(Exception e){
            e.getMessage();
        }
        
    }
    public GUI_delSA(){
        setSize(800,800);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        JLabel lbmas= new JLabel("Mã sách");
        lbmas.setBounds(10, 10, 100, 20);
        JTextField tfmas =new JTextField();
        tfmas.setBounds(150,10,200,20);
        
        JLabel lbtens = new JLabel("Tên sách");
        lbtens.setBounds(10,50,100,20);
        JTextField tftens = new JTextField();
        tftens.setBounds(150,50,200,20);
        
        JLabel lbnamxb = new JLabel("Năm xuất bản");
        lbnamxb.setBounds(10,90,100,20);
        JTextField tfnamxb = new JTextField();
        tfnamxb.setBounds(150,90,200,20);
        
        JLabel lbgiab = new JLabel("Giá bán");
        lbgiab.setBounds(10,140,100,20);
        JTextField tfgiab = new JTextField();
        tfgiab.setBounds(150,140,200,20);
        
        JButton them = new JButton("Thêm");
        them.setBounds(10,230,100,20);
        
        JButton xoa = new JButton("Xóa");
        xoa.setBounds(120,230,100,20);
        
        JButton sua = new JButton("Sửa");
        sua.setBounds(230,230,100,20);
        
        JButton timkiem = new JButton("Tìm kiếm");
        timkiem.setBounds(340,230,100,20);
        
        String column[] = {"Mã sách","Tên sách","Năm xuất bản","Giá bán"};
        tablemodel.setColumnIdentifiers(column);
        
        JTable tb = new JTable(tablemodel);
        JScrollPane jsp = new JScrollPane(tb);
        jsp.setBounds(10,300,760,350);
        
        
        String[] intarray = {"2010","2011","2012","2013","2004"};
        JComboBox<String> cbnamxb = new JComboBox<String>(intarray);
        cbnamxb.setBounds(10,190,100,20);
        
        add(lbmas);
        add(tfmas);
        add(lbtens);
        add(tftens);
        add(lbnamxb);
        add(tfnamxb);
        add(lbgiab);
        add(tfgiab);
        add(them);
        add(xoa);
        add(sua);
        add(timkiem);
        add(jsp);
        add(cbnamxb);
        
        xl.getCon();
        loadData(tablemodel);
        //xử lý click
        tb.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                currentRow = tb.getSelectedRow();
                if(currentRow !=-1){
                    tfmas.setText(tb.getValueAt(currentRow,0).toString());
                    tftens.setText(tb.getValueAt(currentRow,1).toString());
                    tfnamxb.setText(tb.getValueAt(currentRow,2).toString());
                    tfgiab.setText(tb.getValueAt(currentRow,3).toString());
                }
                
            }
        });
        // Them
        them.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                  String mas= tfmas.getText().trim();
                  String tens=tftens.getText().trim();
                  int namxb = Integer.parseInt(tfnamxb.getText().trim());
                  int giab = Integer.parseInt(tfgiab.getText().trim());
                  
                  Sach sach=new Sach(mas,tens,namxb,giab);
                  boolean res = xl.insertSA(sach);
                  if(res){
                      loadData(tablemodel);
                      JOptionPane.showMessageDialog(null, "Them thanh cong");
                  }
                  else{
                      JOptionPane.showMessageDialog(null, "Them that bai");
                  }
            }
        }
        );
        sua.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                  String mas= tfmas.getText().trim();
                  String tens=tftens.getText().trim();
                  int namxb = Integer.parseInt(tfnamxb.getText().trim());
                  int giab = Integer.parseInt(tfgiab.getText().trim());
                  
                  Sach sach=new Sach(mas,tens,namxb,giab);
                  boolean res = xl.updateSA(sach);
                  if(res){
                      loadData(tablemodel);
                      JOptionPane.showMessageDialog(null, "Sua thanh cong");
                  }
                  else{
                      JOptionPane.showMessageDialog(null, "Sua that bai");
                  }
            }
        }
        );
        xoa.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                int confirm = JOptionPane.showConfirmDialog(null, 
                "Bạn có chắc chắn muốn xóa không?", 
                "Xác nhận xóa", 
                JOptionPane.YES_NO_OPTION);
                if(confirm == JOptionPane.YES_OPTION){
                  int namxb = Integer.parseInt(cbnamxb.getSelectedItem().toString());
                   
                  boolean res = xl.deleteSA(namxb);
                  if(res){
                      loadData(tablemodel);
                      JOptionPane.showMessageDialog(null, "Xoa thanh cong");
                  }
                  else{
                      JOptionPane.showMessageDialog(null, "Xoa that bai");
                  }
                }
                else{
                      JOptionPane.showMessageDialog(null, "Xoa that bai");
                  }
            }
        }
        );
        timkiem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                  String mas= tfmas.getText().trim();
                  tablemodel.setRowCount(0);
                  List<Sach> sach;
                  if(mas.isEmpty()){
                      sach = xl.getSA();
                  }
                  else{
                      sach = xl.searchSA(mas);
                  }
                  for(Sach s:sach){
                    Object[] RowsData = {
                        s.getMaS(),
                        s.getTenS(), 
                        s.getNamXB(),
                        s.getGiaB()
                    };
                    tablemodel.addRow(RowsData);
            }
                tablemodel.fireTableDataChanged();
            }
        }
        );
    }
    
    public static void main(String[] args) {
        
        new GUI_delSA().setVisible(true);
        
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    
}
