from Bio import SeqIO

my_file = "TVG.fa"  # Obviously not FASTA


def is_fasta(filename):
    with open(filename, "r") as handle:
        fasta = SeqIO.parse(handle, "fasta")
        return fasta  # False when `fasta` is empty, i.e. wasn't a FASTA file


b = is_fasta(my_file)
for record in SeqIO.parse(my_file, "fasta"):
    print(len(record))

