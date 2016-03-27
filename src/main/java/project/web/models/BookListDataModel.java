package project.web.models;

import java.util.List;
import java.util.Map;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;  
import project.web.beans.Pager;
import project.web.db.DataHelper;
import project.web.entity.ext.BookExt;


public class BookListDataModel extends LazyDataModel<BookExt> {  
    
    private List<BookExt> bookList;
    private DataHelper dataHelper;
    private Pager pager;

    public BookListDataModel(DataHelper dataHelper, Pager pager) {
        this.dataHelper = dataHelper;
        this.pager = pager;
    }
    
    
    @Override  
    public BookExt getRowData(String rowKey) {      
        
        for(BookExt book : bookList) {  
            if(book.getId().intValue() == Long.valueOf(rowKey).intValue())  
                return book;  
        }  
  
        return null;  
    }  
  
    @Override  
    public Object getRowKey(BookExt book) {  
        return book.getId();  
    }  

    
    
  
    @Override  
    public List<BookExt> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {   
        
        pager.setFrom(first);
        pager.setTo(pageSize);
     
        dataHelper.populateList();

        this.setRowCount(pager.getTotalBooksCount().intValue());  
        
        return pager.getList();
        
    }  
}  
