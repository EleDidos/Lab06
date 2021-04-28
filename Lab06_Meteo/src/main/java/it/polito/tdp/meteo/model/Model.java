package it.polito.tdp.meteo.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	
	private List <Rilevamento> rilevamenti;
	private Citta [] myCities = new Citta [3];
	
	private MeteoDAO dao;
	
	int mese=0; //mese passato

	public Model() {
		dao= new MeteoDAO();
		rilevamenti = dao.getAllRilevamenti();
		myCities[0]=new Citta("Genova");
		myCities[1]=new Citta("Milano");
		myCities[2]=new Citta("Torino");
		
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
	 * parziale = "" --> ARRAYLIST (perché devo usare dei GET) con gli oggetti CITTA'
	 * rilevamenti = tutti quelli tra cui cercare prossimo candidato
	 * livello --> n° di città già in soluzione
	 * best = soluzione finale con solo nomi
	 * **/
	public List<Citta> trovaSequenza (int mese){
		List <Citta> best = new ArrayList <>();
		List <Citta> parziale = new ArrayList <>();
		this.mese=mese;
		ricorsiva(parziale,rilevamenti,0,best); //LANCIA LA RICORSIONE
		
		return best;
	} 
	
	
	
	/**
	 * Calcola il COSTO di una determinata soluzione (totale)
	 * @param parziale la soluzione (totale) proposta
	 * @return il valore del costo, che tiene conto delle umidità nei 15 giorni e del costo di cambio città
	 */
	private Double calcolaCosto(List<Citta> parziale) {
		double costo = 0.0;
		
		//sommatoria delle umidità in ciascuna città, considerando il rilevamento del giorno giusto
		//scorrendo parziale con il counter "giorno" che parte da 0
		
		for (int giorno=0; giorno<parziale.size(); giorno++) {
			//dove mi trovo
			Citta c = parziale.get(giorno);
			//che umidità ho in quel giorno in quella città?
			
			for(Rilevamento ri: rilevamenti) {
				LocalDate ld = LocalDate.of(2013,mese,giorno+1);
				if( ri.getLocalita().equals(c.getNome()) && ld.equals(ri.getData()) )
						costo+= ri.getUmidita();
			}
			
		}
		//poi devo sommare 100*numero di volte in cui cambio città
		for (int giorno=0; giorno<parziale.size(); giorno++) {
			//guardo se la città 1 è uguale alla 0, se la 2 è uguale alla 1 e così via...
			if(giorno<14 && !parziale.get(giorno+1).equals(parziale.get(giorno))) {
				costo +=100.0;
			}
		}
		return costo;
	}
	
	
	private void ricorsiva(List <Citta> parziale, List<Rilevamento> rilevamenti, int livello, List <Citta> best) {
		
		/** A: condizione di terminazione --> quando arriva qui, la PARZIALE è una SOLUZIONE DI DIM CORRETTA
		 * ho una sequenza di 15 città  **/
		if (parziale.size()==15) {
			
			/** C: controlliamo se questa parziale mi porta
			 * 		al miglior costo possibile fino ad ora
			 *      **/
				Double costo = calcolaCosto(parziale);
				//Se è la prima parziale che ho trovato o ha un costo minore delle precedenti
				if(best.size()==0 || costo<calcolaCosto(best)) {
					for(Citta ci: parziale)
						best.add(ci);
				}
			
		} else {
			// sono al giorno gg e provo ad aggiungere ognuna delle località per vedere chi mi produce
			// sequenza migliore
			for(Citta prova: myCities) {
				if(aggiuntaValida(prova,parziale)) {
					parziale.add(prova);
					//togli quel rilevamento
					ricorsiva(parziale,rilevamenti,livello+1,best);
					parziale.remove(parziale.size()-1); // tolgo ultimo elemento tornando a 1 liv precente 
														// per provare un'altra combinazione
				}
			}
			
		}
	}
	
	
	/**
	 * Verifica se, data la soluzione {@code parziale} già definita, sia lecito
	 * aggiungere la città {@code prova}, rispettando i vincoli sui numeri giorni
	 * minimi e massimi di permanenza.
	 * 
	 * @param prova la città che sto cercando di aggiungere
	 * @param parziale la sequenza di città già composta
	 * @return {@code true} se {@code prova} è lecita e posso andare avanti di livello, 
	 * 			{@code false} se invece viola qualche vincolo --> torno indietro di liv,
	 * 						  cambio ultima città di prova e ci riprovo
	 *      
	 */
	private boolean aggiuntaValida(Citta prova, List<Citta> parziale) {

		// 1. verifica GG MAX	
		//contiamo quante volte la città 'prova' era già apparsa nell'attuale lista costruita fin qui
		int conta = 0;
		for (Citta precedente:parziale) {
			if (precedente.equals(prova))
				conta++; 
		}
		if (conta >=NUMERO_GIORNI_CITTA_MAX)
			return false; 

		// 2. verifica dei GG MIN
		if (parziale.size()==0) //primo giorno posso inserire qualsiasi città
				return true;
		if (parziale.size()==1 || parziale.size()==2) {
			//siamo al secondo o terzo giorno, non posso cambiare
			//quindi l'aggiunta è valida solo se la città di prova coincide con la sua precedente
			return parziale.get(parziale.size()-1).equals(prova); 
		}
		//nel caso generale, se ho già passato i controlli sopra e quindi sono lì da meno di 6 gg
		//non c'è nulla che mi vieta di rimanere nella stessa città
		//quindi se è uguale alla precedente è OK
		if (parziale.get(parziale.size()-1).equals(prova))
			return true; 
		// se cambio città mi devo assicurare che nei tre giorni precedenti sia rimasto fermo 
		if (parziale.get(parziale.size()-1).equals(parziale.get(parziale.size()-2)) 
		&& parziale.get(parziale.size()-2).equals(parziale.get(parziale.size()-3)))
			return true;

		return false;

	}


			
		

	

} //model
