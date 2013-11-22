grammar StructuredDate;

/*
 * This is a grammar for ANTLR 4 (http://www.antlr.org/).
 *
 * TODO: 
 *   Allow short months ending with periods
 *   Allow ca '.'? for CIRCA
 *   Allow c1970 for CIRCA
 *   Allow year month
 *   Allow YYYY-MM-DD and MM-DD-YYYY
 *
 * To generate the lexer, parser, and listener classes, use the command: 
 *     java -jar /usr/local/lib/antlr-4.1-complete.jar -package org.collectionspace.services.structureddate.antlr StructuredDate.g4
 */

/*
 * Parser rules
 */

oneDisplayDate: displayDate EOF ; 
 
displayDate:    CIRCA year (BCE|AD)?                                   # circaYear
|               year (BCE|AD)?                                         # yearOnly
|               nth QUARTER year                                       # todo
|               LAST QUARTER year                                      # todo
|               nth HALF year                                          # todo
|               LAST HALF year                                         # todo
|               year HYPHEN year                                       # todo
|               CIRCA year HYPHEN year                                 # todo
|               CIRCA year HYPHEN year BCE                             # todo
|               CIRCA year BCE HYPHEN year BCE                         # todo
|               CIRCA year BCE HYPHEN year                             # todo
|               CIRCA year BCE HYPHEN year AD                          # todo
|               year BCE HYPHEN year BCE                               # todo
|               year HYPHEN year BCE                                   # todo
|               year BCE HYPHEN year                                   # todo
|               year BCE HYPHEN year AD                                # todo
|               season year                                            # todo
|               season HYPHEN season year                              # todo
|               season year HYPHEN season year                         # todo
|               season year BCE                                        # todo
|               partOf year                                            # todo
|               partOf year BCE                                        # todo
|               month                                                  # todo
|               month HYPHEN month                                     # todo
|               partOf singleDate BCE                                  # todo
|               singleDate BCE                                         # todo
|               partOf singleDate                                      # todo
|               singleDate                                             # singleDateOnly
|               singleDate HYPHEN singleDate                           # todo
|               nth CENTURY                                            # todo
|               CIRCA nth CENTURY                                      # todo
|               nth CENTURY AD                                         # todo
|               nth CENTURY BCE                                        # todo
|               CIRCA nth CENTURY BCE                                  # todo
|               nth CENTURY HYPHEN nth CENTURY                         # todo
|               nth HYPHEN nth CENTURY                                 # todo
|               nth HYPHEN nth CENTURY BCE                             # todo
|               nth CENTURY BCE HYPHEN nth CENTURY BCE                 # todo
|               nth CENTURY BCE HYPHEN nth CENTURY                     # todo
|               nth CENTURY BCE HYPHEN nth CENTURY AD                  # todo
|               nth QUARTER nth CENTURY                                # todo
|               nth QUARTER nth CENTURY HYPHEN nth QUARTER nth CENTURY # todo
|               LAST QUARTER nth CENTURY                               # todo
|               nth HALF nth CENTURY                                   # todo
|               LAST HALF nth CENTURY                                  # todo
|               partOf nth CENTURY                                     # todo
|               partOf nth CENTURY BCE                                 # todo
|               partOf nth CENTURY HYPHEN partOf nth CENTURY           # todo
|               partOf nth HYPHEN partOf nth CENTURY                   # todo
|               partOf nth HYPHEN nth CENTURY                          # todo
|               partOf nth CENTURY BCE HYPHEN partOf nth CENTURY BCE   # todo
|               partOf nth CENTURY BCE HYPHEN partOf nth CENTURY       # todo
|               century                                                # todo
|               partOf century                                         # todo
|               partOf century HYPHEN partOf century                   # todo
|               partOf century BCE HYPHEN partOf century BCE           # todo
|               partOf century BCE                                     # todo
|               CIRCA century                                          # todo
|               CIRCA century BCE                                      # todo
|               nth MILLENIUM                                          # todo
|               nth MILLENIUM BCE                                      # todo
|               decade                                                 # todo
|               decade HYPHEN decade                                   # todo
|               decade HYPHEN partOf decade                            # todo
|               partOf decade HYPHEN partOf century                    # todo
|               partOf decade HYPHEN partOf decade                     # todo
|               partOf decade HYPHEN decade                            # todo
|               partOf decade                                          # todo
|               decade BCE                                             # todo
|               partOf decade BCE                                      # todo
|               CIRCA decade                                           # todo
|               CIRCA decade BCE                                       # todo
|               dateRange                                              # dateRangeOnly
;

dateRange:      monthOnlyRange
|               strDateRange
|               numDateRange
;              

singleDate:     numDate
|               strDate
|               invStrDate
;

month:          monthOnly ;

strDate:        strMonth dayOfMonth COMMA? year ;
invStrDate:     year COMMA? strMonth dayOfMonth ;
strDateRange:   strMonth dayOfMonth HYPHEN dayOfMonth COMMA? year ;
monthOnlyRange: strMonth HYPHEN strMonth COMMA? year ;
numDateRange:   numMonth SLASH dayOfMonth HYPHEN dayOfMonth SLASH year ;
numDate:        numMonth SLASH dayOfMonth SLASH year ;
monthOnly:      strMonth COMMA? year ;
decade:         TENS ;
century:        HUNDREDS ;
season:         SPRING | SUMMER | WINTER | FALL ;
partOf:         EARLY | MIDDLE | LATE | BEFORE | AFTER ;
nth:            NTHSTR | FIRST | SECOND | THIRD | FOURTH ;
strMonth:       MONTH ;
year:           NUMBER ;
numMonth:       NUMBER ;
dayOfMonth:     NUMBER ;


/*
 * Lexer rules
 */

WS:             [ \t\r\n]+ -> skip;
CIRCA:          'c' ('irca' | '.') ;
SPRING:         'spring' ;
SUMMER:         'summer' ;
WINTER:         'winter' ;
FALL:           'fall' | 'autumn' ;
EARLY:          'early' ;
MIDDLE:         'mid' ;
LATE:           'late' ;
BEFORE:         'before' | 'pre' '-'? ;
AFTER:          'after' | 'post' '-'?;
FIRST:          'first' ;
SECOND:         'second' ;
THIRD:          'third' ;
FOURTH:         'fourth' ;
LAST:           'last' ;
QUARTER:        'quarter' ;
HALF:           'half' ;
CENTURY:        'century' ;
MILLENIUM:      'millenium' ;
MONTH:          'january' | 'february' | 'march' | 'april' | 'may' | 'june' | 'july' | 'august' | 'september' | 'october' | 'november' | 'december' |
                'jan' | 'feb' | 'mar' | 'apr' | 'jun' | 'jul' | 'aug' | 'sep' | 'sept' | 'oct' | 'nov' | 'dec' ;
BCE:            'bc' 'e'? |  'b.c.' | 'b.c.e.' ;
AD:             'ad' | 'a.d.' | 'ce' | 'c.e.';
STRING:         [a-z\.]+ ;
NTHSTR:         [0-9]*? ([0456789] 'th' | '1st' | '2nd' | '3rd' | '11th' | '12th' | '13th') ;
HUNDREDS:       [0-9]*? '00' S;
TENS:           [0-9]*? '0' S;
NUMBER:         [0-9]+ ;
COMMA:          ',' ;
HYPHEN:         '-' ;
SLASH:          '/' ;

fragment 
S:              '\''? 's' ;
