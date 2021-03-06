package snytng.astah.plugin.readplus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.change_vision.jude.api.inf.presentation.IPresentation;

//読み上げ結果の構造体
public class MessagePresentation {

	// 読み上げられたメッセージ
	List<String> messages = null;

	// 読み上げられた要素のIPresentation
	List<IPresentation> presentations = null;

	public MessagePresentation(){
		clear();
	}
	
	public void clear(){
		this.messages = new ArrayList<>();
		this.presentations = new ArrayList<>();
	}
	
	public MessagePresentation(String[] ms, IPresentation[] ps){
		this.messages = Arrays.asList(ms);
		this.presentations = Arrays.asList(ps);
	}
	
	public void add(String m, IPresentation p){
		this.messages.add(m);
		this.presentations.add(p);
	}
	
	public void addAll(MessagePresentation mp){
		this.messages.addAll(mp.messages);
		this.presentations.addAll(mp.presentations);		
	}
	
	public String[] getMessagesArray(){
		return this.messages.toArray(new String[messages.size()]);
	}
	
	public String[] getPresentationsArray(){
		return this.presentations.toArray(new String[presentations.size()]);
	}

}