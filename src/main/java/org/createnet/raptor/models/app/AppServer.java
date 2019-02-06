package org.createnet.raptor.models.app;

import java.io.Serializable;

import javax.persistence.Column;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppServer implements Serializable {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1000000000000001L;
	
	protected String name;
    protected String url;
    protected String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(length = 128)
    @Size(min = 4, max = 128)
    protected String password;

    protected String login;
    protected String appid;
    protected String mqtt;
    protected String topic;
    protected String posturl;
    protected String getUrl;

    public AppServer() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getMqtt() {
        return mqtt;
    }

    public void setMqtt(String mqtt) {
        this.mqtt = mqtt;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPosturl() {
        return posturl;
    }

    public void setPosturl(String posturl) {
        this.posturl = posturl;
    }

    public String getGetUrl() {
        return getUrl;
    }

    public void setGetUrl(String getUrl) {
        this.getUrl = getUrl;
    }
}
