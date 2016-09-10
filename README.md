# TennisMixerMatcherAlgorithm
UNDER CONSTRUCTION algorithm for MixUp App.

This is the working algorithm for the MixUp.
 * This algorithm minimizes the occurrence of individuals playing in repeat groups
 * and maximizes the chance that individuals play with others they have not played with
 * or have played with the least number of times. It also tries to ensure that there
 * are an equal number of either sex on competing teams.
 * 
 * It does so by keeping a table of records for the number of times a given pair of players
 * have played either as partners or opponents. This is their pair "score". Then the program
 * creates sets of proposed "test instances" or sets of random groups. From these random 
 * groups, the program selects the test instance with the lowest score, thus sorting all 
 * the players into groups. After the players have been thus sorted into groups, the program
 * then tests which pairs of players have the highest difference in the number of times they
 * have played as partners and as opponents. If they have played as partners more times than
 * as opponents, they will be sorted into opposite teams, or vice versa, until the teams are
 * full.
 
UNDER CONSTRUCTION.
 * Currently does not handle cases where there are an insufficient number of people to fill 
 * all the positions, and some other cases.
 * This program is optimized for tennis mixers where a team of a f and m pair up
 * against another team of a f and m for each court.
