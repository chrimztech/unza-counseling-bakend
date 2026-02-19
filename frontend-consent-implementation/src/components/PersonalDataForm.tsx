import React, { useState } from 'react';
import personalDataFormService, { 
  PersonalDataFormRequest, 
  PersonalDataFormResponse, 
  PreviousCounselingType, 
  ReferralSource, 
  CounselingReason 
} from '../services/personalDataFormService';

interface PersonalDataFormProps {
  clientId: number;
  onSuccess: (form: PersonalDataFormResponse) => void;
  onError: (error: any) => void;
}

const PersonalDataForm: React.FC<PersonalDataFormProps> = ({ clientId, onSuccess, onError }) => {
  const [formData, setFormData] = useState<PersonalDataFormRequest>({
    dateOfInterview: new Date().toISOString(),
    gender: 'PREFER_NOT_TO_SAY',
    yearOfBirth: new Date().getFullYear(),
    school: '',
    computerNo: '',
    yearOfStudy: 1,
    occupation: '',
    contactAddress: '',
    phoneNumber: '',
    maritalStatus: 'SINGLE',
    previousCounseling: [],
    otherPreviousCounseling: '',
    referralSources: [],
    otherReferralSource: '',
    counselingReasons: [],
    otherCounselingReason: '',
    familyMembers: [],
    goodHealth: true,
    healthCondition: '',
    takingMedication: false,
    medicationDetails: '',
    additionalInformation: ''
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value, type, checked } = e.target;

    if (type === 'checkbox') {
      // Handle array fields
      if (name === 'previousCounseling') {
        setFormData(prev => {
          const current = prev.previousCounseling || [];
          return {
            ...prev,
            previousCounseling: checked 
              ? [...current, value as PreviousCounselingType]
              : current.filter(item => item !== value)
          };
        });
      } else if (name === 'referralSources') {
        setFormData(prev => {
          const current = prev.referralSources || [];
          return {
            ...prev,
            referralSources: checked 
              ? [...current, value as ReferralSource]
              : current.filter(item => item !== value)
          };
        });
      } else if (name === 'counselingReasons') {
        setFormData(prev => {
          const current = prev.counselingReasons || [];
          return {
            ...prev,
            counselingReasons: checked 
              ? [...current, value as CounselingReason]
              : current.filter(item => item !== value)
          };
        });
      }
    } else if (type === 'boolean') {
      setFormData(prev => ({ ...prev, [name]: checked }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      const response = await personalDataFormService.createPersonalDataForm(clientId, formData);
      onSuccess(response);
    } catch (error) {
      onError(error);
    }
  };

  return (
    <div className="max-w-4xl mx-auto bg-white rounded-lg shadow-md p-6">
      <h2 className="text-2xl font-bold mb-6 text-gray-800">THE UNIVERSITY OF ZAMBIA COUNSELLING SERVICE</h2>
      <h3 className="text-xl font-semibold mb-4 text-gray-700">PERSONAL DATA FORM</h3>
      
      <p className="mb-6 text-gray-600">
        Please answer the following questions as completely as possible. The information will be useful to the Counsellor and it will be kept in confidence.
      </p>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Personal Details */}
        <section>
          <h4 className="text-lg font-semibold mb-4 text-gray-700">PERSONAL DETAILS</h4>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">CLIENT/FILE NO:</label>
              <input
                type="text"
                name="clientFileNo"
                value={formData.clientFileNo || ''}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Date of Interview:</label>
              <input
                type="datetime-local"
                name="dateOfInterview"
                value={formData.dateOfInterview?.slice(0, 16) || ''}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Gender:</label>
              <select
                name="gender"
                value={formData.gender}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              >
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
                <option value="PREFER_NOT_TO_SAY">Prefer not to say</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Year of Birth:</label>
              <input
                type="number"
                name="yearOfBirth"
                value={formData.yearOfBirth}
                onChange={handleChange}
                min="1900"
                max={new Date().getFullYear()}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">School:</label>
              <input
                type="text"
                name="school"
                value={formData.school}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Computer No:</label>
              <input
                type="text"
                name="computerNo"
                value={formData.computerNo}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Year of Study:</label>
              <input
                type="number"
                name="yearOfStudy"
                value={formData.yearOfStudy}
                onChange={handleChange}
                min="1"
                max="6"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Occupation:</label>
              <input
                type="text"
                name="occupation"
                value={formData.occupation}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Contact Address:</label>
              <input
                type="text"
                name="contactAddress"
                value={formData.contactAddress}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Phone Number:</label>
              <input
                type="tel"
                name="phoneNumber"
                value={formData.phoneNumber}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>
        </section>

        {/* Marital Status */}
        <section>
          <h4 className="text-lg font-semibold mb-4 text-gray-700">MARITAL STATUS</h4>
          <div className="space-y-2">
            {['SINGLE', 'MARRIED', 'DIVORCED', 'SEPARATED', 'WIDOWED', 'LIVING_TOGETHER'].map((status) => (
              <label key={status} className="inline-flex items-center mr-6">
                <input
                  type="radio"
                  name="maritalStatus"
                  value={status}
                  checked={formData.maritalStatus === status}
                  onChange={handleChange}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500"
                />
                <span className="ml-2 text-sm text-gray-700 capitalize">{status.replace('_', ' ')}</span>
              </label>
            ))}
          </div>
        </section>

        {/* Previous Counseling */}
        <section>
          <h4 className="text-lg font-semibold mb-4 text-gray-700">PREVIOUS COUNSELING</h4>
          <div className="space-y-2">
            {Object.values(PreviousCounselingType).map((type) => (
              <label key={type} className="flex items-center">
                <input
                  type="checkbox"
                  name="previousCounseling"
                  value={type}
                  checked={formData.previousCounseling?.includes(type)}
                  onChange={handleChange}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500"
                />
                <span className="ml-2 text-sm text-gray-700 capitalize">{type.replace('_', ' ')}</span>
              </label>
            ))}
          </div>

          {formData.previousCounseling?.includes(PreviousCounselingType.OTHER) && (
            <div className="mt-2">
              <label className="block text-sm font-medium text-gray-700 mb-2">Other (specify):</label>
              <input
                type="text"
                name="otherPreviousCounseling"
                value={formData.otherPreviousCounseling}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          )}
        </section>

        {/* Referral Source */}
        <section>
          <h4 className="text-lg font-semibold mb-4 text-gray-700">REFERRED BY</h4>
          <div className="space-y-2">
            {Object.values(ReferralSource).map((source) => (
              <label key={source} className="flex items-center">
                <input
                  type="checkbox"
                  name="referralSources"
                  value={source}
                  checked={formData.referralSources?.includes(source)}
                  onChange={handleChange}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500"
                />
                <span className="ml-2 text-sm text-gray-700 capitalize">{source.replace('_', ' ')}</span>
              </label>
            ))}
          </div>

          {formData.referralSources?.includes(ReferralSource.OTHER) && (
            <div className="mt-2">
              <label className="block text-sm font-medium text-gray-700 mb-2">Other (specify):</label>
              <input
                type="text"
                name="otherReferralSource"
                value={formData.otherReferralSource}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          )}
        </section>

        {/* Reasons for Seeking Counseling */}
        <section>
          <h4 className="text-lg font-semibold mb-4 text-gray-700">REASONS FOR SEEKING COUNSELING</h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {Object.values(CounselingReason).map((reason) => (
              <label key={reason} className="flex items-center">
                <input
                  type="checkbox"
                  name="counselingReasons"
                  value={reason}
                  checked={formData.counselingReasons?.includes(reason)}
                  onChange={handleChange}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500"
                />
                <span className="ml-2 text-sm text-gray-700 capitalize">{reason.replace('_', ' ')}</span>
              </label>
            ))}
          </div>

          <div className="mt-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">Other (specify):</label>
            <input
              type="text"
              name="otherCounselingReason"
              value={formData.otherCounselingReason}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </section>

        {/* Family History */}
        <section>
          <h4 className="text-lg font-semibold mb-4 text-gray-700">FAMILY HISTORY</h4>
          <p className="mb-4 text-gray-600">List members of your immediate family/guardian (e.g., Father, Mother, Uncle/Aunt, Siblings, Spouse, Children including dependants):</p>
          
          <div className="border rounded-md p-4 mb-4">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b">
                  <th className="text-left py-2 px-3 font-semibold text-gray-700">NAME</th>
                  <th className="text-left py-2 px-3 font-semibold text-gray-700">RELATIONSHIP</th>
                  <th className="text-left py-2 px-3 font-semibold text-gray-700">AGE</th>
                  <th className="text-left py-2 px-3 font-semibold text-gray-700">LEVEL OF EDUCATION</th>
                  <th className="text-left py-2 px-3 font-semibold text-gray-700">OCCUPATION</th>
                </tr>
              </thead>
              <tbody>
                {[0, 1, 2].map((index) => (
                  <tr key={index} className="border-b">
                    <td className="py-2 px-3">
                      <input
                        type="text"
                        placeholder="Name"
                        className="w-full px-2 py-1 border border-gray-300 rounded text-sm"
                      />
                    </td>
                    <td className="py-2 px-3">
                      <input
                        type="text"
                        placeholder="Relationship"
                        className="w-full px-2 py-1 border border-gray-300 rounded text-sm"
                      />
                    </td>
                    <td className="py-2 px-3">
                      <input
                        type="number"
                        placeholder="Age"
                        className="w-full px-2 py-1 border border-gray-300 rounded text-sm"
                      />
                    </td>
                    <td className="py-2 px-3">
                      <input
                        type="text"
                        placeholder="Education"
                        className="w-full px-2 py-1 border border-gray-300 rounded text-sm"
                      />
                    </td>
                    <td className="py-2 px-3">
                      <input
                        type="text"
                        placeholder="Occupation"
                        className="w-full px-2 py-1 border border-gray-300 rounded text-sm"
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        {/* Health */}
        <section>
          <h4 className="text-lg font-semibold mb-4 text-gray-700">HEALTH</h4>
          
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Are you in good health?</label>
              <div className="flex space-x-4">
                <label className="flex items-center">
                  <input
                    type="radio"
                    name="goodHealth"
                    value="true"
                    checked={formData.goodHealth === true}
                    onChange={(e) => handleChange({ ...e, target: { name: 'goodHealth', type: 'boolean', checked: true } } as any)}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="ml-2 text-sm text-gray-700">YES</span>
                </label>
                <label className="flex items-center">
                  <input
                    type="radio"
                    name="goodHealth"
                    value="false"
                    checked={formData.goodHealth === false}
                    onChange={(e) => handleChange({ ...e, target: { name: 'goodHealth', type: 'boolean', checked: false } } as any)}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="ml-2 text-sm text-gray-700">NO</span>
                </label>
              </div>
            </div>

            {formData.goodHealth === false && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">If not, please describe your condition:</label>
                <textarea
                  name="healthCondition"
                  value={formData.healthCondition}
                  onChange={handleChange}
                  rows={4}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Are you taking medication?</label>
              <div className="flex space-x-4">
                <label className="flex items-center">
                  <input
                    type="radio"
                    name="takingMedication"
                    value="true"
                    checked={formData.takingMedication === true}
                    onChange={(e) => handleChange({ ...e, target: { name: 'takingMedication', type: 'boolean', checked: true } } as any)}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="ml-2 text-sm text-gray-700">YES</span>
                </label>
                <label className="flex items-center">
                  <input
                    type="radio"
                    name="takingMedication"
                    value="false"
                    checked={formData.takingMedication === false}
                    onChange={(e) => handleChange({ ...e, target: { name: 'takingMedication', type: 'boolean', checked: false } } as any)}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="ml-2 text-sm text-gray-700">NO</span>
                </label>
              </div>
            </div>

            {formData.takingMedication === true && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">If yes, please specify (include contraceptive pills, over-the-counter medication, psychiatric drugs, etc.):</label>
                <textarea
                  name="medicationDetails"
                  value={formData.medicationDetails}
                  onChange={handleChange}
                  rows={4}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            )}
          </div>
        </section>

        {/* Additional Information */}
        <section>
          <h4 className="text-lg font-semibold mb-4 text-gray-700">ADDITIONAL INFORMATION</h4>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Please indicate additional information about yourself that is important for the Counsellor to know:
          </label>
          <textarea
            name="additionalInformation"
            value={formData.additionalInformation}
            onChange={handleChange}
            rows={6}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </section>

        {/* Submit Button */}
        <div className="flex justify-end">
          <button
            type="submit"
            className="px-6 py-2 bg-blue-600 text-white font-medium rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            Submit Form
          </button>
        </div>
      </form>
    </div>
  );
};

export default PersonalDataForm;
