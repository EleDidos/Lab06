package it.polito.tdp.meteo.model;

import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.anagrammi.model.Esito;
import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	
	private  int t = 0; //cnt per torino
	private  int m = 0; //milano
	private  int g = 0; //genova
	private int consecutivi = 0; //gg consecutivi in ultima città
	private String lastCity ="";
	
	private MeteoDAO dao;

	public Model() {
		dao= new MeteoDAO();
	}

	
	public StringBuilder getUmiditaMedia(int mese) {
		StringBuilder sb = new StringBuilder ();
		//c'è un rilevamento per ogni città con di fianco la media delle umidità nel mese passato 
		for(Rilevamento ri: dao.getUmiditaMediaMese(mese)) {
			sb.append(String.format("%-10s %-4d\n", ri.getLocalita(),ri.getUmidita()));
		}
		return sb;
	}
	
	
	/** PROCEDURA PUBBLICA 
	 * parziale = "" --> ARRAYLIST (perché devo usare dei GET)
	 * rilevamenti = tutti quelli tra cui cercare prossimo candidato
	 * livello = 0 --> n° di città già in soluzione
	 * seq = soluzione finale 
	 * **/
	public StringBuilder trovaSequenza (int mese){
		StringBuilder seq = new StringBuilder() ;
		String [] parziale = new String [15];
		ricorsiva(parziale,dao.getAllRilevamenti(),0,seq); //LANCIA LA RICORSIONE
		return seq;
	} 
	
	private int aggiornaCNT (String localita) {
		
			switch(localita) {
				case ("Torino"): t++;
					return t;
				case ("Milano"): m++;
					return m;
				case ("Genova"): g++;
					return g;
				default:
					return -1;
			}
	}
	
	
	private void ricorsiva(String[] parziale, List<Rilevamento> rilevamenti, int livello, StringBuilder seq) {
		
		/** A: condizione di terminazione --> quando arriva qui, la PARZIALE è una SOLUZIONE DI DIM CORRETTA
		 * ho una sequenza di 15 città  **/
		if (parziale.length==15) {
			int i=1;
			
			/** C: devo però controllare che questa soluzione sia valida
			 *     - almeno 1 gg in ogni città
			 *      **/
			if(t>=1  && m>=1  && g>=1 ) { //GIUSTA e mi fermo
				for(String citta: parziale)
					seq.append(String.format("%2-d %-10s", i++, citta));
				return;
			}
			
		} else {
			//pos = posti rimanenti da visitare
			for(int pos=0; pos<(15-parziale.length); pos++) {
				
				// umidità min da aggiornare a ogni giro
				float min =1000;
				
				for(Rilevamento ri: rilevamenti) { //tra i rimasti
					
					// 1. se l'umidità < min
					if(ri.getUmidita()<min) {
						
						// 2.A gg consecutivi in una città<3
						if(consecutivi<3) {
							if(ri.getLocalita().compareTo(lastCity)==0) {
								parziale[pos]=lastCity; // se è uguale 
								consecutivi++;
								min=ri.getUmidita();
								this.aggiornaCNT(lastCity);
								rilevamenti.remove(pos);
								/*-->*/ricorsiva(parziale,rilevamenti,livello++,seq);
							} else {
								parziale[pos]=ri.getLocalita(); // se non è uguale 
								consecutivi=1; //va a 1
								min=ri.getUmidita();
								lastCity=ri.getLocalita(); //cambia
								this.aggiornaCNT(lastCity);
								rilevamenti.remove(pos);
								/*-->*/ricorsiva(parziale,rilevamenti,livello++,seq);
							}	
						} else {
							//2.B gg consecutivi>=3, check se tot della città<6
							if(this.aggiornaCNT(lastCity)<6) {
								//ho aggiornato il cnt in auto solo chimando la f di aggiornamento
								//perché posso ancora stare qui
								parziale[pos]=lastCity; 
								consecutivi++;
								min=ri.getUmidita();
								rilevamenti.remove(pos);
								/*-->*/ricorsiva(parziale,rilevamenti,livello++,seq);
							} else {
								//se sono già a 6 gg in questa città devo cambiare per forza
								consecutivi=0;
								//vado avanti a cercare un nuovo min
							}
						}
						
					} // umidità min
					
					
				}//for
				
				
		
		

	

			}
