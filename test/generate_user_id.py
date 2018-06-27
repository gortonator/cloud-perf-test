import csv
with open('user_ids.csv', 'w', newline='') as csvfile:    
    for i in range(100):
        spamwriter = csv.writer(csvfile)
        spamwriter.writerow(["user" + str(i)])
