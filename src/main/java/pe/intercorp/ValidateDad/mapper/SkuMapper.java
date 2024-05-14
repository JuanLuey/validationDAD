package pe.intercorp.ValidateDad.mapper;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import pe.intercorp.ValidateDad.entity.Sku;

public class SkuMapper implements RowMapper<Sku> {

    @Override
    @Nullable
    public Sku mapRow(ResultSet rs, int rowNum) throws SQLException {        
        var result = new Sku();
        result.setSku(rs.getString("sku"));
        result.setMarca(rs.getString("marca"));
        result.setCodbar(rs.getString("codbar"));
        return result;
    }
    
   
}
