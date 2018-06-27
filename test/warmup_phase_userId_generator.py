import csv
import random
with open('warmup_user_ids.csv', 'w', newline='') as csvfile:    
    for i in range(20):
        id = random.randint(0,20)
        spamwriter = csv.writer(csvfile)
        spamwriter.writerow(["user" + str(id)])
