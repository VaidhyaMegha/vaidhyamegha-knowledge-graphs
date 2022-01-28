-- public.trial_article definition

-- Drop table

-- DROP TABLE public.trial_article;

CREATE TABLE public.trial_article (
	id varchar NOT NULL DEFAULT nextval('id_trial_article_seq'::regclass),
	trial varchar NULL,
	pubmed_articles _int4 NULL,
	CONSTRAINT trial_article_pkey PRIMARY KEY (id),
	CONSTRAINT trial_article_trial_key UNIQUE (trial)
);
CREATE INDEX id_trial_article_idx ON public.trial_article USING btree (trial);
