-- trigger function

-- DROP FUNCTION public.menu_items_notify();

-- N.B.: adeguare la dichiarazione delle variabili per gli id delle categorie dei panini da considerare, sulla base di quanto definito nella tabella tipologie
CREATE OR REPLACE FUNCTION public.menu_items_notify()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
declare
	panini_cat_id INTEGER := 1;
    piadine_cat_id INTEGER := 2;
    toast_cat_id INTEGER := 3;
    piatti_unici_cat_id INTEGER := 6;
    panini_spec_cat_id INTEGER := 8;
	isValidMenuItem INTEGER = 0;
	json_result json;
	v_txt text;
BEGIN
	RAISE NOTICE 'Nuova riga inserita nella tabella righe_articoli con id: %', NEW.id;

	-- verifico se articolo inserito appartiene a una delle categorie gestibili dalle piastre
	select count(ra.id) into isValidMenuItem
    from righe_articoli ra
    inner join tipologie t on ra.desc_tipologia = t.descrizione
    where ra.id = new.id and t.id in (panini_cat_id, piadine_cat_id, toast_cat_id, piatti_unici_cat_id, panini_spec_cat_id);

	if isValidMenuItem > 0 then
		select json_build_object(
        'id', ra.id,
        'orderNumber', o.progressivo, 'tableNumber', o."numeroTavolo", 'date', o."data", 'time', o.ora, 'clientName', o.cliente,
        'takeAway', o.esportazione, 'orderNotes', o.note,
        'quantity', r.quantita,
        'menuItemId', a.id, 'menuItemName', a.descrizione, 'menuItemNotes', ra.note,
        'categoryId', t.id, 'categoryName', t.descrizione
        )
        into json_result
        from righe_articoli ra
        inner join righe r on r.id = ra.id_riga
        inner join articoli a on r.descrizionebreve = a.descrizionebreve
        inner join tipologie t on a.id_tipologia = t.id
        inner join ordini o on o.id = r.id_ordine
        where ra.id = new.id;

	  	RAISE NOTICE 'Risultato JSON %', json_result;

	   	v_txt := format('{"operation": "%s","item": %s }', TG_OP, json_result);
	  	RAISE NOTICE 'Payload notifica %', v_txt;

	    EXECUTE FORMAT('NOTIFY plate_orders, ''%s''', v_txt);
	else
		RAISE NOTICE 'Categoria riga articolo non valida: %', NEW.pos_tipologia;
	end if;

	RETURN NEW;
END;
$function$
;

-- trigger
--DROP TRIGGER righe_articoli_trigger ON public.righe_articoli;

CREATE TRIGGER righe_articoli_trigger
after INSERT OR UPDATE ON righe_articoli
FOR EACH ROW
EXECUTE PROCEDURE menu_items_notify();