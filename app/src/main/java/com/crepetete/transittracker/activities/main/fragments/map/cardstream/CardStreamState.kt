package com.crepetete.transittracker.activities.main.fragments.map.cardstream

class CardStreamState(val visibleCards: Array<Card>,
                      val hiddenCards: Array<Card>,
                      val dismissibleCards: HashSet<String>,
                      val shownTag: String?)