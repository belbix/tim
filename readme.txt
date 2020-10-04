./mysqldump -u belbix -p111111 tim ticks --where="server = 'bitmex' and symbol = 'XBTUSD' and date > '2018-06-01' and date < '2019-01-01 00:00:00'" --no-create-info --skip-extended-insert > 'c:\\workspace\\tim\\ticks2018-1.sql'


2 years
0/0/1 - 6454
0.25/0.25/1 - 6221
