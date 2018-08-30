SELECT
    ss.specimen_id,
    ss.MIN_USEFUL_LEVEL,
    sus.marked_seen,
    SUS.MIN_UNANSWERED_LEVEL,
    sus.marked_unusable,
    ss.unusable_validated
from
    h_contribution_specimen_stat ss
    left outer join h_contribution_specimen_user_stat sus
    on ss.specimen_id = sus.specimen_id and sus.user_id = 1000288
where
    ss.MIN_USEFUL_LEVEL > SUS.MIN_UNANSWERED_LEVEL
    or ss.min_useful_level > 4
    or ss.unusable_validated != 0

-- stats full pour utilisateur
select
    ss.specimen_id,
    ss.MIN_USEFUL_LEVEL,
    sus.user_id,
    sus.marked_seen,
    SUS.MIN_UNANSWERED_LEVEL,
    sus.marked_unusable,
    ss.unusable_validated
from
   h_contribution_specimen_stat ss
   left outer join h_contribution_specimen_user_stat sus
   on ss.specimen_id = sus.specimen_id and user_id = 1000288
where
   ss.mission_id = 1007083 and
(   sus.marked_seen = 1       -- utilisateur n'est pas passé sur la planche
or sus.marked_unusable = 1    -- utilisateur ne l'a pas marqué inutilisable
or ss.unusable_validated = 1  -- pas validéé comme inutilisable
or ss.min_useful_level is null -- identique specimen complet
or ss.min_useful_level > 3     -- niveau trop haut
or (sus.user_id is not null and sus.min_unanswered_level is null) -- utilisateur a répondu à tout
);




-- Tirage specimen utile
select
  *
from h_specimen s
where
  s.mission_id = 1007083 and
  id not in (

select
    ss.specimen_id
from
   h_contribution_specimen_stat ss
   left outer join h_contribution_specimen_user_stat sus
   on ss.specimen_id = sus.specimen_id and user_id = 1000288
where
   ss.mission_id = 1007083 and
(   sus.marked_seen = 1       -- utilisateur n'est pas passé sur la planche
or sus.marked_unusable = 1    -- utilisateur ne l'a pas marqué inutilisable
or ss.unusable_validated = 1  -- pas validéé comme inutilisable
or ss.min_useful_level is null -- identique specimen complet
or ss.min_useful_level > 3     -- niveau trop haut
or (sus.user_id is not null and sus.min_unanswered_level is null) -- utilisateur a répondu à tout
)

);