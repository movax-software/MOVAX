#include <stdio.h>

void bubbleSort(int nums[], int length);
void quickSort(int nums[], int inicio, int fin);

int main(){
    int x = 255 && 0;
    printf("%d",  x);
    return 0;
}

void bubbleSort(int nums[], int length){    
    
    for (int i = 0; i < length-1; i++)
    for (int j = 0; j < length-1; j++) {
        if (nums[j] > nums[j+1]) {
            int temp = nums[j];
            nums[j] = nums[j+1];
            nums[j+1] = temp;
        }
    } 

}

void quickSort(int nums[], int inicio, int fin) {
    if (inicio >= fin) 
        return;
    
    int i = inicio - 1;
    int pivote = nums[fin];

    for (int j = inicio; j < fin; j++) {
        if (nums[j] <= pivote) {
            i++;
            int temp = nums[i];
            nums[i] = nums[j];
            nums[j] = temp;
        }
    }
    
    int indicePivote = i + 1;
    int temp = nums[indicePivote];
    nums[indicePivote]  = nums[fin];
    nums[fin] = temp;

    quickSort(nums, inicio, indicePivote - 1);
    quickSort(nums, indicePivote + 1, fin);
}
