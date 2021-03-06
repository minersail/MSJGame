package woohoo.gameworld.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import java.util.ArrayList;

public class DialogueComponent implements Component
{
    private ArrayList<DialogueLine> sequence;
    private int index;
    
	/**
	 * Holds all the dialogue component for an NPC, unless event tag is true.
	 * If event tag is true, holds all the dialogue for an event.
	 * @param id ID to locate the dialogue in XML
	 * @param cutscene Whether this is for an NPC or an event
	 */
	public DialogueComponent(int id, boolean cutscene)
    {
        sequence = new ArrayList<>();
        
        FileHandle handle = Gdx.files.local(cutscene ? "data/cutscenedialogue.xml" : "data/dialogue.xml");
        
        XmlReader xml = new XmlReader();
        Element root = xml.parse(handle.readString());       
        Element dialogue = root.getChild(id);
        
        for (Element e : dialogue.getChildrenByName("line"))
        {            
            sequence.add(new DialogueLine(e.getAttributes()));
        }
		
		// -1 represents the beginning state
		index = -1; // The first interaction with the NPC will advance index to 0
    }
    
    public DialogueLine getCurrentLine()
    {
        if (index < sequence.size())
            return sequence.get(index);
        else
            return null; //sequence.get(sequence.size() - 1);
    }
	
	public void advance()
	{
		index++;
	}
	
	/*
	Advances the index to the given choice
	*/
	public void advanceChoice(int choice)
	{
		int i = index;
		while (choice >= 0) // Skip towards to the picked choice
		{
			i++;
			if (sequence.get(i).name().equals("Choice"))
				choice--;
		}
		
		index = i;
	}
	
	public void advanceToChoiceEnd()
	{
		while (!(sequence.get(index).id() == -1 && sequence.get(index).text().equals("ENDCHOICE")))
		{
			index++;
		}
	}
	
	/*
	Re-sets the dialogue to the previous break, or to beginning if none are found
	*/
	public void loop()
	{
		boolean foundLoop = false;
		for (int i = index - 1; i >= 0; i--)
		{
			if (sequence.get(i).id() == -1 && sequence.get(i).text.equals("BREAK"))
			{
				index = i;
				foundLoop = true;
			}
		}
		
		if (!foundLoop) index = -1;
	}
	
	/**
	 * Given the index of a starting choice, return all choices' strings until the end choice
	 * @return array of strings of the choices
	 */
	public Array<String> getChoices()
	{
		if (sequence.get(index).getInt("choices") == -1)
		{
			Gdx.app.error("ERROR", "Dialogue index is not at a choice start");
			return null;
		}
		
		Array<String> choices = new Array<>();
		
		int i = index;
		// Search until the end choice for all choice options
		while (!(sequence.get(i).id() == -1 && sequence.get(i).text().equals("ENDCHOICE")))
		{
			if (sequence.get(i).id() == -1 && sequence.get(i).name().equals("Choice"))
			{
				choices.add(sequence.get(i).text());
			}
			i++;
		}
		
		return choices;
	}
	
	public class DialogueLine
    {        
		private String name;
		private String text;
		private int id;
        
        private ObjectMap extraData;
        
        public DialogueLine(ObjectMap map)
		{
			name = (String)map.remove("name");
            text = (String)map.remove("text");
            id = Integer.parseInt((String)map.remove("character"));
            
            extraData = map;
		}
		
		public String name() { return name; }
		public String text() { return text; }
		public int id() { return id; }
		public int getInt(String identifier) 
        { 
            if (extraData.containsKey(identifier))
                return Integer.parseInt((String)extraData.get(identifier));
            
            Gdx.app.error("ERROR", "Identifier " + identifier + " not found in dialogue line.");
            return -1;
        }
	}
}