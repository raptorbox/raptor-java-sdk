package org.createnet.raptor.models.app;

import javax.persistence.Id;
import java.io.Serializable;

public class PayloadCodec implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1000000000000001L;
	
	@Id
    protected String id;
    protected String name;
    protected String codec;

//    private String[] CodecType = new String[] {"inhouse-lpp", "cayenne-lpp", "custom-function"};
    
    public PayloadCodec() {
    	
    }

    public PayloadCodec(String name, String codec) {
        this.id = name.replaceAll("\\s", "-");
        this.name = name;
        this.codec = codec;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }
}
