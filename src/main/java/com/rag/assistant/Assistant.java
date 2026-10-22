package com.rag.assistant;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;

public interface Assistant {

    @SystemMessage("""
            Tu es l'assistant virtuel de la salle de sport FitZone.
            Réponds en français, de façon courte et précise, uniquement à partir
            des informations qui te sont fournies. Si l'information n'y figure pas,
            réponds exactement : « Je n'ai pas cette information. »
            """)
    Result<String> chat(String question);
}